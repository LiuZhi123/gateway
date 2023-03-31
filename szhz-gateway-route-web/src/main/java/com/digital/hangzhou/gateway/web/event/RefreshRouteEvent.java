package com.digital.hangzhou.gateway.web.event;

import com.custom.starters.customwebspringbootstarters.core.exceptions.CommonException;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.web.cache.LocalCacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.ArrayList;
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

    public void saveAndNotify(RouteDefinition definition) {
        try {
            //前置过滤不合法的URI
            UriComponentsBuilder.fromUri(definition.getUri()).build(false).toUri();
            this.routeDefinitionWriter.save(Mono.just(definition)).subscribe();
            notifyChanged();
            //发布redis通知，所有节点接收通知更新内存中的路由
            redisTemplate.convertAndSend(RedisConstant.ADD_ROUTES_CHANNEL, definition.getId());
        } catch (Exception e) {
            log.error("过滤不合法的URI， routeId=" + definition.getId() + ",URI=" + definition.getUri());
            throw new CommonException(70001, "创建路由:" + definition.getId() + "失败，URI地址不合法，请检查");
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
        try {
            //前置过滤不合法的URI
            UriComponentsBuilder.fromUri(routeDefinition.getUri()).build(false).toUri();
            this.routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
            notifyChanged();
        } catch (Exception e) {
            log.error("过滤不合法的URI， routeId=" + routeDefinition.getId() + ",URI=" + routeDefinition.getUri());
            throw new CommonException(70001, "创建路由:" + routeDefinition.getId() + "失败，URI地址不合法，请检查");
        }
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
        List<RouteDefinition> result = new ArrayList<>(routeDefinitionSet.size());
        routeDefinitionSet.stream().forEach(r->{
            try {
                //前置过滤不合法的URI
                UriComponentsBuilder.fromUri(r.getUri()).build(false).toUri();
                result.add(r);
            } catch (Exception e) {
                log.error("过滤不合法的URI， routeId=" + r.getId() + ",URI=" + r.getUri());
            }
        });
        result.stream().forEach(r->{
            this.routeDefinitionWriter.save(Mono.just(r)).subscribe();
            LocalCacheRepository.ROUTE_DEFINITION_CACHE.put(r.getId(), r);
        });
        notifyChanged();
    }


    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
