package com.digital.hangzhou.gateway.web.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class RefreshRouteEvent implements ApplicationEventPublisherAware {

    @Resource
    private RouteDefinitionWriter routeDefinitionWriter;

    private ApplicationEventPublisher applicationEventPublisher;

    private void notifyChanged() {
        this.applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
    }

    /**
     * 增加路由
     *
     */
    public void save(RouteDefinition definition) {
        try {
            routeDefinitionWriter.save(Mono.just(definition)).subscribe();
            notifyChanged();
        } catch (Exception e) {
            log.error("update route fail， 【routeId={}】,error message: {}", definition.getId(), e);
        }
    }

    /**
     * 删除路由
     *
     */
    public void delete(String id) {
        try {
            routeDefinitionWriter.delete(Mono.just(id)).subscribe();
            notifyChanged();
        } catch (Exception e) {
            log.error("delete fail,not find route  【routeId={}】,error message: {}：", id, e);
        }
    }

    /**
     * 批量增加路由
     *
     */
    public void saveBatch(List<RouteDefinition> routeDefinitionList){
        routeDefinitionList.stream().forEach(r->routeDefinitionWriter.save(Mono.just(r)).subscribe());
        notifyChanged();
    }


    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
