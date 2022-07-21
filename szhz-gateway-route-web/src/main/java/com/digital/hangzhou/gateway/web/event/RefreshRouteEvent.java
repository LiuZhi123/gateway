package com.digital.hangzhou.gateway.web.event;

import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.web.cache.LocalCacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class RefreshRouteEvent implements ApplicationEventPublisherAware {

    @Resource
    private RouteDefinitionWriter routeDefinitionWriter;

    @Resource
    private RedisTemplate redisTemplate;

    private ApplicationEventPublisher applicationEventPublisher;

    private void notifyChanged() {
        this.applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));

    }

    /**
     * 增加路由并发送通知
     *
     */
    public void saveAndNotify(RouteDefinition definition) {
        try {
            this.routeDefinitionWriter.save(Mono.just(definition)).subscribe();
            notifyChanged();
            //发布redis通知，所有节点接收通知更新内存中的路由
            redisTemplate.convertAndSend(RedisConstant.ADD_ROUTES_CHANNEL, definition.getId());
        } catch (Exception e) {
            log.error("update route fail， 【routeId={}】,error message: {}", definition.getId(), e);
        }
    }

    /**
     * 删除路由
     *
     */
    public void deleteAndNotify(String id) {
        try {
            this.routeDefinitionWriter.delete(Mono.just(id)).subscribe();
            notifyChanged();
            redisTemplate.convertAndSend(RedisConstant.DELETE_ROUTES_CHANNEL, id);
        } catch (Exception e) {
            log.error("delete fail,not find route  【routeId={}】,error message: {}：", id, e);
        }
    }

    /**
     * 增加路由并刷新
     * @param routeDefinition
     */
    public void save(RouteDefinition routeDefinition){
        this.routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
        notifyChanged();
    }

    /**
     * 删除路由并刷新
     */
    public void delete(String id) {
        this.routeDefinitionWriter.delete(Mono.just(id)).subscribe();
        notifyChanged();
    }

    /**
     * 批量增加路由
     *
     */
    public void saveBatch(List<RouteDefinition> routeDefinitionSet){
        routeDefinitionSet.stream().forEach(r->this.routeDefinitionWriter.save(Mono.just(r)).subscribe());
        notifyChanged();
        LocalCacheRepository.ROUTE_DEFINITION_CACHE.putAll(redisTemplate.opsForHash().entries(RedisConstant.ROUTE_KEY));
    }


    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
