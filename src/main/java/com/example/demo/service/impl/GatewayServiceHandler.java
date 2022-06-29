package com.example.demo.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
//加载顺序
@Order(1)
//时间发布
public class GatewayServiceHandler implements ApplicationEventPublisherAware, CommandLineRunner {
    private static final Log log = LogFactory.getLog(GatewayServiceHandler.class);
    private ApplicationEventPublisher applicationEventPublisher;
    private final RouteDefinitionWriter routeDefinitionWriter;

    @Autowired
    public GatewayServiceHandler(RouteDefinitionWriter routeDefinitionWriter){
        this.routeDefinitionWriter = routeDefinitionWriter;
    }

    @Override
    public void run(String... args) throws Exception {
        this.loudRouteConfig();
    }

    public String loudRouteConfig(){
        log.info("------开始加载路由配置文件");
        RouteDefinition routeDefinition = new RouteDefinition();
        //从数据库中加载路由信息到缓存中
        List<RouteDefinition> list = new ArrayList<>();
        //发送数据推送之消费者消费
        Mono.just(list).subscribe();
        //
        this.applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
        return "susses";
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
