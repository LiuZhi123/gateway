package com.digital.hangzhou.gateway.web.filter;

import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.HttpStatusHolder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.setResponseStatus;

@Slf4j
@Order(4)
@Component
public class RedirectGatewayFilterFactory extends AbstractGatewayFilterFactory {
    @Override
    public GatewayFilter apply(Object config) {
         return new  GatewayFilter(){
             @Override
             public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                 HttpStatusHolder httpStatus = HttpStatusHolder.parse("302");
                 String url = exchange.getRequest().getURI().toString();
                 Route route = (Route) exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                 String routeId = route.getId();
                 String basePath = route.getMetadata().get("path").toString();
                 //对原始请求地址进行分割,获取系统定义的http://localhost:8080/HTML_xxx后面部分的内容,防   止漏参数
                 String redirectPath = url.split(routeId,2)[1];
                 if (StrUtil.endWith(basePath ,CharPool.SLASH)){
                     //界面配置中配置的链接地址应为下游的host地址，http://localhost1:8081/,不需要配置多余的数据，否则在此处拼接的地址会与期望跳转                                                                                                                            地址有出入
                     basePath = StrUtil.sub(basePath, 0,basePath.length()-1);
                 }
                 if (!exchange.getResponse().isCommitted()) {
                     setResponseStatus(exchange, httpStatus);
                     final ServerHttpResponse response = exchange.getResponse();
                     response.getHeaders().set(HttpHeaders.LOCATION, new StringBuilder(basePath).append(redirectPath).toString());
                     return response.setComplete();
                 }
                 return Mono.empty();
             }
         };
    }
}
