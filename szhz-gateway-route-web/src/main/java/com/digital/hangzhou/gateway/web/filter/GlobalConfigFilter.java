package com.digital.hangzhou.gateway.web.filter;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.digital.hangzhou.gateway.common.constant.ApiConstant;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.common.domain.dto.GlobalConfig;
import com.digital.hangzhou.gateway.web.exception.ErrorHandler;
import com.digital.hangzhou.gateway.web.exception.ErrorMessage;
import com.digital.hangzhou.gateway.web.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * @author lz
 * @date 2022/12/1 10:10
 */
@Component
@Slf4j
public class GlobalConfigFilter implements GlobalFilter, Ordered {

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (null != redisTemplate.opsForValue().get(RedisConstant.GATEWAY_GLOBAL_CONFIG)){
            GlobalConfig config = JSON.parseObject(JSONObject.toJSONString(redisTemplate.opsForValue().get(RedisConstant.GATEWAY_GLOBAL_CONFIG)), GlobalConfig.class);
            if (null != config.getWhiteIpStatus() &&  config.getWhiteIpStatus() == true){
                Set<String> whiteIps = config.getWhiteIpSet();
                if (CollUtil.isEmpty(whiteIps)){
                    return ErrorHandler.writeFailedToResponse(exchange.getResponse(), ErrorMessage.HTTP_ERROR_403);
                }
                ServerHttpRequest request = exchange.getRequest();
                String remoteIp = IpUtil.getRemoteIp(request);
                if (!whiteIps.contains(remoteIp)){
                    return ErrorHandler.writeFailedToResponse(exchange.getResponse(), ErrorMessage.HTTP_ERROR_403);
                }
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
