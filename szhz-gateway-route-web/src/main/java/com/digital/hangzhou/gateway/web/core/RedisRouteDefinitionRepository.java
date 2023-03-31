package com.digital.hangzhou.gateway.web.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import com.digital.hangzhou.gateway.web.cache.LocalCacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RedisRouteDefinitionRepository implements RouteDefinitionRepository {

    private  List<String> allowFilters = CollUtil.newArrayList(RouteInfoConstant.CONSUMER_PREDICATE_FACTORY,
            RouteInfoConstant.ADD_REQUEST_HEADER_GATEWAY_FILTER, RouteInfoConstant.MONITOR_GATEWAY_FILTER,
            RouteInfoConstant.REDIRECT_GATEWAY_FILTER, RouteInfoConstant.REWRITE_PATH_GATEWAY_FILTER,
            RouteInfoConstant.STRIP_PREFIX_GATEWAY_FILTER, RouteInfoConstant.WHITE_IP_PREDICATE_FACTORY,
            RouteInfoConstant.SIGN_FACTORY, RouteInfoConstant.REFRESH_TOKEN_FACTORY);

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(LocalCacheRepository.ROUTE_DEFINITION_CACHE.values());
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(r->{
            if (CollUtil.isEmpty(r.getMetadata())){
                log.info("异常路由数据,缺少元数据:" + r.getId());
                return Mono.never();
            }
            if (null == r.getPredicates() || CollUtil.isEmpty(r.getPredicates())){
                log.info("异常路由数据,断言列表为空:" + r.getId());
                return Mono.never();
            }
            Set<String> filterNames  = r.getFilters().stream().map(e->e.getName()).collect(Collectors.toSet());
            if (!CollUtil.containsAll(allowFilters, filterNames)){
                log.info("异常路由数据,存在不合法的过滤器:" + r.getId());
                return Mono.never();
            }
            redisTemplate.opsForHash().put(RedisConstant.ROUTE_KEY,r.getId(),r);
            LocalCacheRepository.ROUTE_DEFINITION_CACHE.put(r.getId(),r);
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(r->{
            if (StrUtil.isNotBlank(r)){
                LocalCacheRepository.ROUTE_DEFINITION_CACHE.remove(r);
                redisTemplate.opsForHash().delete(RedisConstant.ROUTE_KEY, r);
                return Mono.empty();
            }
            return Mono.defer(()-> Mono.error(new NotFoundException("根据路由ID查找路由失败: " + r)));
        });
    }

    public void saveBatch(List<RouteDefinition> routeDefinitionList){
        if(CollUtil.isNotEmpty(routeDefinitionList)){
//            refreshRouteEvent.saveBatch(routeDefinitionList);
        }
    }
}
