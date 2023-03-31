package com.digital.hangzhou.gateway.web.filter;

import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.crypto.symmetric.SM4;
import com.digital.hangzhou.gateway.common.constant.CommonConstant;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.web.exception.ErrorHandler;
import com.digital.hangzhou.gateway.web.exception.ErrorMessage;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;;import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * ak/sk 鉴权模式的验签过滤器
 * @author lz
 * @date 2023/3/7 9:56
 */
@Component
public class SignGatewayFilterFactory extends AbstractGatewayFilterFactory {
    @Resource
    private RedisTemplate redisTemplate;

    private static final String decryptKey = "18ea5c4afd2367b6";

    private static final SM4 sm4 = new SM4(decryptKey.getBytes());

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, filterChain)->{
            //指定从header中获取appKey,sign,time
            Route route = (Route)exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders headers = request.getHeaders();
            if (!headers.containsKey(CommonConstant.APP_KEY) || !headers.containsKey(CommonConstant.SIGN) || !headers.containsKey(CommonConstant.TIME))
                return ErrorHandler.writeFailedToResponse(response, ErrorMessage.HEADER_ERROR);
            String appKey = headers.get(CommonConstant.APP_KEY).get(0);
            String sign = headers.get(CommonConstant.SIGN).get(0);
            String time = headers.get(CommonConstant.TIME).get(0);
            //对sign进行SM4解密，解密失败直接返回异常
            String[] origin;
            String originAppKey, originSecretKey, expireString, originAppCode;
            LocalDateTime currentTime, expireTime;
            try {
                origin = sm4.decryptStr(sign).split(StrUtil.AT);
                originAppCode = origin[0];
                originAppKey = origin[1];
                originSecretKey = origin[2];
                expireString = origin[3];
            }
            catch (Exception e){
                return ErrorHandler.writeFailedToResponse(response, ErrorMessage.SIGN_ERROR);
            }
            //验证访问的应用是不是申请过该API
            Object o = redisTemplate.opsForHash().get(RedisConstant.SZHZ_Sign_PREFIX_KEYS + route.getId(), originAppCode);
            if (null == o)
                return ErrorHandler.writeFailedToResponse(response, ErrorMessage.APP_INFO_IS_NOT_AUTHORED_BY_API);
            try {
                 currentTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.valueOf(time)), ZoneId.of("+8"));
                 if (currentTime.isBefore(LocalDateTime.now().minusMinutes(15)))
                     throw new RuntimeException();
            }
            catch (Exception e){
                return ErrorHandler.writeFailedToResponse(response, ErrorMessage.TIME_ERROR);
            }
            expireTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.valueOf(expireString)), ZoneId.of("+8"));
            //比较时间确认提供的SIGN是否过期
            if (currentTime.isAfter(expireTime))
                return ErrorHandler.writeFailedToResponse(response, ErrorMessage.SIGN_EXPIRED);
            //比较ak,sk是否正确，将解析出来的sk与redis中存储的MD5值进行比较,结果相同则认为sk一致
            if (!StrUtil.equals(appKey, originAppKey))
                return ErrorHandler.writeFailedToResponse(response, ErrorMessage.SIGN_ERROR);
            String md5SecretKey = MD5.create().digestHex(originSecretKey);
            if (!StrUtil.equals(md5SecretKey, o.toString()))
                return ErrorHandler.writeFailedToResponse(response, ErrorMessage.SIGN_ERROR);
            return filterChain.filter(exchange);
        };
    }

}
