package com.digital.hangzhou.gateway.web.component;

import cn.hutool.core.text.CharPool;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import com.digital.hangzhou.gateway.common.request.ReleaseRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.InMemoryRouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


@Service
public class GatewayServiceHandler implements ApplicationEventPublisherAware ,CommandLineRunner {

//    @Resource
//    private RedisRouteDefinitionRepository redisRouteDefinitionRepository;

    @Resource
    private RouteDefinitionWriter routeDefinitionWriter;

    @Resource
    private RedisTemplate redisTemplate;

    private ApplicationEventPublisher applicationEventPublisher;

    //容器启动后去首先去缓存和数据库加载路由信息
    @Override
    public void run(String... args) throws Exception {
        System.out.println("------------------>开始加载路由信息");
        Set<String> keys = redisTemplate.keys(RedisConstant.ROUTE_PREFIX);
        //从缓存中加载路由信息
        List<RouteDefinition> routeDefinitionList = redisTemplate.opsForValue().multiGet(keys);
        routeDefinitionList.stream().forEach(e->routeDefinitionWriter.save(Mono.just(e)));
        //路由刷新事件
        this.applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
        System.out.println("动态路由初始化加载完毕");
    }

    //接收从Controller来的信息，上架网关接口
    public void save(ReleaseRequest request){
        //根据Api编号查询路由的消费者信息以及IP白名单信息模板信息以及限流信息生成对应的routeDefinition对象
        RouteDefinition routeDefinition = new RouteDefinition();
        String url = request.getServiceProtocol().name() + CharPool.COLON + CharPool.SLASH + CharPool.SLASH
                + request.getServiceAddress() + CharPool.COLON + request.getServicePort() + request.getFullPath();
        routeDefinition.setId(request.getApiCode());
        try {
            routeDefinition.setUri(new URI(url));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Map<String, Object> metaData = new HashMap(1){
            {
                put(RouteInfoConstant.ORG_CODE, request.getMainOrgCode());
            }
        };
        routeDefinition.setMetadata(metaData);
        routeDefinition.setPredicates(getPredicateList(request));
        routeDefinition.setFilters(getFilterDefinition());
        //保存路由信息至内存
        routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
        redisTemplate.opsForValue().set(RedisConstant.ROUTE_PREFIX + routeDefinition.getId(), routeDefinition);
//        this.applicationEventPublisher.publishEvent(new RefreshRoutesEvent(routeDefinitionWriter));
    }


    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }


    private List<PredicateDefinition> getPredicateList(ReleaseRequest request){
        List<PredicateDefinition> predicateDefinitions = new ArrayList<>();
        switch (request.getAuthType()){
            case PUBLIC:
                break;
            case DISABLE:
                PredicateDefinition consumer = new PredicateDefinition();
                consumer.setName(RouteInfoConstant.CONSUMER_PREDICATE_FACTORY);
                consumer.setArgs(new HashMap(){{
                    put("consumers", request.getConsumerList());
                }});
                predicateDefinitions.add(consumer);
            case AUTHOR:
                PredicateDefinition author = new PredicateDefinition();
                author.setName(RouteInfoConstant.CONSUMER_PREDICATE_FACTORY);
                author.setArgs(new HashMap(){{
                    put("consumers", request.getConsumerList());
                }});
                predicateDefinitions.add(author);
                //白名单断言工程
                PredicateDefinition whiteIpList = new PredicateDefinition();
                whiteIpList.setName(RouteInfoConstant.WHITE_IP_PREDICATE_FACTORY);
                predicateDefinitions.add(whiteIpList);
        }
        return predicateDefinitions;
    }


    public List<FilterDefinition> getFilterDefinition(){
        List<FilterDefinition> filterDefinitionList = new ArrayList<>();
        FilterDefinition stripPrefix = new FilterDefinition();
        stripPrefix.setName(RouteInfoConstant.STRIP_PREFIX_GATEWAY_FILTER);
        stripPrefix.addArg("parts","1");
        filterDefinitionList.add(stripPrefix);
        return filterDefinitionList;
    }
}
