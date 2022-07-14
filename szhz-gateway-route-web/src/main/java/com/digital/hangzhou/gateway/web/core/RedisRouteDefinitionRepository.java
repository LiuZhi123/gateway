package com.digital.hangzhou.gateway.web.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.web.cache.LocalCacheRepository;
import com.digital.hangzhou.gateway.web.event.RefreshRouteEvent;
import com.digital.hangzhou.gateway.web.util.SentinelRuleUtil;
import lombok.Data;
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
import java.util.Set;

import static java.util.Collections.synchronizedMap;

@Data
@Component
public class RedisRouteDefinitionRepository implements RouteDefinitionRepository {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RefreshRouteEvent refreshRouteEvent;

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(LocalCacheRepository.ROUTE_DEFINITION_CACHE.values());
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(r->{
            if (StrUtil.isBlank(r.getId())){
                return Mono.error(new NotFoundException("路由数据已存在，请检查后重新提交!"));
            }
            redisTemplate.opsForHash().put(RedisConstant.ROUTE_KEY,r.getId(),r);
            LocalCacheRepository.ROUTE_DEFINITION_CACHE.put(r.getId(),r);
            refreshRouteEvent.saveAndNotify(r);
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(r->{
            if (StrUtil.isNotBlank(r)){
                LocalCacheRepository.ROUTE_DEFINITION_CACHE.remove(r);
                refreshRouteEvent.deleteAndNotify(r);
                redisTemplate.opsForHash().delete(RedisConstant.ROUTE_KEY, r);
                return Mono.empty();
            }
            return Mono.defer(()-> Mono.error(new NotFoundException("根据路由ID查找路由失败: " + r)));
        });
    }

    public void saveBatch(List<RouteDefinition> routeDefinitionList){
        if(CollUtil.isNotEmpty(routeDefinitionList)){
            refreshRouteEvent.saveBatch(routeDefinitionList);
        }
    }
}
