package com.digital.hangzhou.gateway.web.filter;

import cn.hutool.core.collection.CollUtil;
import com.custom.starters.customwebspringbootstarters.core.exceptions.IErrorCode;
import com.custom.starters.customwebspringbootstarters.util.Assert;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import com.digital.hangzhou.gateway.web.exception.ErrorHandler;
import com.digital.hangzhou.gateway.web.exception.ErrorMessage;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * 白名单过滤器
 */
@Order(3)
@Component
public class WhiteIpGatewayFilterFactory extends AbstractGatewayFilterFactory {
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public GatewayFilter apply(Object config) {
        return ((exchange, chain) -> {
            Route route = (Route) exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            ServerHttpResponse response = exchange.getResponse();
            String remoteIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();
            List<String> appCode = params.get(RouteInfoConstant.API_KEY);
            if (CollUtil.isEmpty(appCode)){
                appCode = exchange.getRequest().getHeaders().get(RouteInfoConstant.API_KEY);
            }
            //todo IP白名单考虑多网卡情况
            Set<String> whiteIpList =  (Set<String>) redisTemplate.opsForHash().get(route.getId(), appCode.get(0));
            if (null == whiteIpList || !whiteIpList.contains(remoteIp)){
                ErrorHandler.writeFailedToResponse(response, ErrorMessage.HTTP_ERROR_403);
            }
            return chain.filter(exchange);
        });
    }
}
