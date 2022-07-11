package com.digital.hangzhou.gateway.web.component;

import cn.hutool.core.util.StrUtil;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.synchronizedMap;


//@Component
public class RedisRouteDefinitionRepository implements RouteDefinitionRepository {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RouteDefinitionLocator locator;


    private final Map<String, RouteDefinition> routes = synchronizedMap(
            new LinkedHashMap<String, RouteDefinition>());

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(routes.values());
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {

        return route.flatMap(r->{
            if (StrUtil.isEmpty(r.getId())){
                return Mono.error(new NotFoundException("no route Id found"));
            }
            routes.put(r.getId(),r);
            redisTemplate.opsForValue().set(r.getId(),r);
            //todo  发送redis通知键事件
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(r->{
            if (true){
                //判空处理，如果routeId为空则抛出异常
                routes.remove(routeId);
                redisTemplate.delete(routeId);
                //todo  发送redis通知键事件
                return Mono.empty();
            }
            return Mono.defer(()-> Mono.error(new NotFoundException("根据路由ID查找路由失败: " + routeId)));
        });

    }


}
