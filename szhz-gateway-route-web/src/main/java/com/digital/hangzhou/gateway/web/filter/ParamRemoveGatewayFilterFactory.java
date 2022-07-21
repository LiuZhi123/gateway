package com.digital.hangzhou.gateway.web.filter;

import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;


import static org.springframework.util.CollectionUtils.unmodifiableMultiValueMap;

@Order(Ordered.LOWEST_PRECEDENCE)
//去除消费者参数的全局过滤器
@Component
public class ParamRemoveGatewayFilterFactory implements GlobalFilter{


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>(request.getQueryParams());
        HttpHeaders headers = request.getHeaders();
        if (queryParams.containsKey(RouteInfoConstant.API_KEY)) {
            queryParams.remove(RouteInfoConstant.API_KEY);
            URI newUri = UriComponentsBuilder.fromUri(request.getURI())
                    .replaceQueryParams(unmodifiableMultiValueMap(queryParams)).build().toUri();
            request = exchange.getRequest().mutate().uri(newUri).build();
            exchange = exchange.mutate().request(request).build();
        }
        if (headers.containsKey(RouteInfoConstant.API_KEY)) {
            request = exchange.getRequest().mutate().headers(httpHeaders -> httpHeaders.remove(RouteInfoConstant.API_KEY)).build();
            exchange = exchange.mutate().request(request).build();
        }
        return chain.filter(exchange);
    }
}
