package com.digital.hangzhou.gateway.web.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;


//网关监控日志记录
@Component
public class MonitorGatewayFilterFactory extends AbstractGatewayFilterFactory {
    @Override
    public GatewayFilter apply(Object config) {
        return ((exchange, chain) -> {
            //todo 监控完善
            ServerHttpRequest request = exchange.getRequest();
            return chain.filter(exchange);
        });
    }
}
