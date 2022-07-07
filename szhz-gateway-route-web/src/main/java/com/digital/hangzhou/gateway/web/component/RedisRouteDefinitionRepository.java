package com.digital.hangzhou.gateway.web.component;

import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Component
public class RedisRouteDefinitionRepository implements RouteDefinitionRepository {
    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return null;
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {

        return null;
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {

        return null;
    }


}
