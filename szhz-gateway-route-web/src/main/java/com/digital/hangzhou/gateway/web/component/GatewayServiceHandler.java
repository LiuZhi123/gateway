package com.digital.hangzhou.gateway.web.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.ArrayList;

import java.util.List;


@Service
public class GatewayServiceHandler implements ApplicationEventPublisherAware ,CommandLineRunner {

    @Resource
    private RedisRouteDefinitionRepository redisRouteDefinitionRepository;

    private ApplicationEventPublisher applicationEventPublisher;

    //容器启动后去首先去缓存和数据库加载路由信息
    @Override
    public void run(String... args) throws Exception {
        System.out.println("------------------>开始加载路由信息");
        //从缓存中加载路由信息
        List<RouteDefinition> list = new ArrayList();
//        list.stream().forEach(e->repository.save(Mono.just(e)));
    }

    //接收从Controller来的信息，上架网关接口
    public void save(String apiCode){
        //根据Api编号查询路由的消费者信息以及IP白名单信息模板信息以及限流信息生成对应的routeDefinition对象
        RouteDefinition routeDefinition = new RouteDefinition();
        //保存路由信息至内存
//        repository.save(Mono.just(routeDefinition));
//        this.applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
        //缓存至redis

        //触发redis键更新通知
    }



    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
