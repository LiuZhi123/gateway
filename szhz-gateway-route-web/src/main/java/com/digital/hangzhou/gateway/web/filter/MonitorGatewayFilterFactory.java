package com.digital.hangzhou.gateway.web.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
//网关转发监控日志记录
@Component
public class MonitorGatewayFilterFactory extends AbstractGatewayFilterFactory {

    @Override
    public GatewayFilter apply(Object config) {
        return ((exchange, chain) -> {
           return chain.filter(exchange);
        });
    }
}
