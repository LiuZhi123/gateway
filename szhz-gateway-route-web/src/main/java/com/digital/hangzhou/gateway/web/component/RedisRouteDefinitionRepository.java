package com.digital.hangzhou.gateway.web.component;

import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;


@Component
public class RedisRouteDefinitionRepository implements RouteDefinitionRepository {
    @Resource
    private RouteDefinitionLocator locator;
    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return locator.getRouteDefinitions();
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        //todo redis存储，发送redis通知键事件
        return Mono.empty();
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(r->{
            if (true){
                //判空处理，如果routeId为空则抛出异常
                return Mono.empty();
            }
            return Mono.defer(()-> Mono.error(new NotFoundException("根据路由ID查找路由失败: " + routeId)));
        });

    }


}
