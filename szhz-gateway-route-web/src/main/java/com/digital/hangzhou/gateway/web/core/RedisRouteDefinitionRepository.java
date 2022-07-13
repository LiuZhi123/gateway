package com.digital.hangzhou.gateway.web.core;

import cn.hutool.core.util.StrUtil;
import com.digital.hangzhou.gateway.web.event.RefreshRouteEvent;
import com.digital.hangzhou.gateway.web.util.SentinelRuleUtil;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.synchronizedMap;


@Component
public class RedisRouteDefinitionRepository implements RouteDefinitionRepository {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RefreshRouteEvent refreshRouteEvent;

    private final Map<String, RouteDefinition> routes = synchronizedMap(
            new LinkedHashMap<String, RouteDefinition>());

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(routes.values());
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {

        return route.flatMap(r->{
            if (StrUtil.isBlank(r.getId())){
                return Mono.error(new NotFoundException("路由数据已存在，请检查后重新提交!"));
            }
            refreshRouteEvent.save(r);
            routes.put(r.getId(),r);
            redisTemplate.opsForValue().set(r.getId(),r);
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(r->{
            if (StrUtil.isNotBlank(r)){
                //判空处理，如果routeId为空则抛出异常
                refreshRouteEvent.delete(r);
                routes.remove(r);
                redisTemplate.delete(r);
                return Mono.empty();
            }
            return Mono.defer(()-> Mono.error(new NotFoundException("根据路由ID查找路由失败: " + routeId)));
        });
    }

    public void saveBatch(List<RouteDefinition> routeDefinitionList){
        refreshRouteEvent.saveBatch(routeDefinitionList);
    }
}
