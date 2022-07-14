package com.digital.hangzhou.gateway.web.config;

import cn.hutool.core.collection.CollUtil;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.web.listener.AddRouteListener;
import com.digital.hangzhou.gateway.web.listener.RefreshSentinelRulesListener;
import com.digital.hangzhou.gateway.web.predicate.ConsumerPredicateFactory;
import com.digital.hangzhou.gateway.web.predicate.WhiteIpPredicateFactory;
import com.digital.hangzhou.gateway.web.listener.DelRouteListener;

import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;


import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Executor;

@Configuration
public class GatewayConfig {
    @Resource
    private AddRouteListener addRouteListener;

    @Resource
    private DelRouteListener delRouteListener;

    @Resource
    private RefreshSentinelRulesListener refreshSentinelRulesListener;

    @Resource
    private Executor gatewayExecutor;


//    自定义路由管理器，自定义的断言工厂与过滤器工厂需要在此处添加
    @Bean
    public RouteLocator customRouteLocator(GatewayProperties properties, List<GatewayFilterFactory> gatewayFilterFactories,
                                     List<RoutePredicateFactory> predicateFactories, RouteDefinitionLocator routeDefinitionLocator,
                                     ConfigurationService service){
        //增加自定义断言工厂
        predicateFactories.addAll(CollUtil.newArrayList(new ConsumerPredicateFactory(), new WhiteIpPredicateFactory()));
        return new RouteDefinitionRouteLocator(routeDefinitionLocator, predicateFactories, gatewayFilterFactories, properties, service);
    }





    //redis消息监听器
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory){
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        //添加指定的消息监听器和监听键
        redisMessageListenerContainer.addMessageListener(addRouteListener, new ChannelTopic(RedisConstant.ADD_ROUTES_CHANNEL));
        redisMessageListenerContainer.addMessageListener(delRouteListener, new ChannelTopic(RedisConstant.DELETE_ROUTES_CHANNEL));
        redisMessageListenerContainer.addMessageListener(refreshSentinelRulesListener, new ChannelTopic(RedisConstant.REFRESH_SENTINEL_CHANNEL));
        //配置自定义线程池处理消息
        redisMessageListenerContainer.setTaskExecutor(gatewayExecutor);
        return  redisMessageListenerContainer;
    }
}
