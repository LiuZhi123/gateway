package com.digital.hangzhou.gateway.web.filter;

import cn.hutool.core.text.CharPool;
import cn.hutool.core.text.StrPool;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson.JSONObject;
import com.digital.hangzhou.gateway.web.exception.ErrorHandler;
import com.digital.hangzhou.gateway.web.exception.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lz
 * @date 2023/3/23 15:38
 */
@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RefreshTokenGatewayFilterFactory extends AbstractGatewayFilterFactory<RefreshTokenGatewayFilterFactory.Config> {

    @Resource
    private RestTemplate restTemplate;

    private MD5 md5 = MD5.create();

    public RefreshTokenGatewayFilterFactory(){
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            String refreshTokenUrl = "https://interface.zjzwfw.gov.cn/gateway/app/refreshTokenByKey.htm?";
            String appKey = config.getAppKey();
            String secretKey = config.getSecretKey();
            Long current = System.currentTimeMillis();
            String refreshSign = md5.digestHex(appKey + secretKey + current);
            Map<String, String> postForRefreshToken = new HashMap<>();
            postForRefreshToken.put("appKey", appKey);
            postForRefreshToken.put("requestTime", current.toString());
            try {
                //调用省平台接口获取请求密钥，根据响应code判断是否成功
                refreshTokenUrl = new StringBuilder(refreshTokenUrl).append("appKey=").append(appKey).append("&sign=")
                        .append(refreshSign).append("&requestTime=").append(current).toString();
                String res = restTemplate.postForObject(refreshTokenUrl,null, String.class);
                JSONObject jsonRes = JSONObject.parseObject(res);
                if (jsonRes.getString("code").equals("00")){
                    JSONObject datas = jsonRes.getJSONObject("datas");
                    String requestSecret = datas.getString("requestSecret");
                    //appKey,requestSecret,current再次进行md5作为请求接口sign
                    String signBuilder = new StringBuilder(appKey).append(requestSecret).append(current).toString();
                    String sign = md5.digestHex(signBuilder);
                    postForRefreshToken.put("sign", sign);
                    //重新包装请求
                    ServerHttpRequest request = exchange.getRequest().mutate().headers(httpHeaders -> {
                        httpHeaders.setAll(postForRefreshToken);
                    }).build();
                    exchange = exchange.mutate().request(request).build();
                }else{
                    log.error(res);
                    throw new RuntimeException();
                }
            }
            catch (Exception e){
                return ErrorHandler.writeFailedToResponse(exchange.getResponse(), ErrorMessage.REFRESH_TOKEN_REQUEST_BAD);
            }
            return chain.filter(exchange);
        });
    }


    protected static class Config{

        private String appKey;

        private String secretKey;

        public String getAppKey() {
            return appKey;
        }

        public void setAppKey(String appKey) {
            this.appKey = appKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }
    }
}
