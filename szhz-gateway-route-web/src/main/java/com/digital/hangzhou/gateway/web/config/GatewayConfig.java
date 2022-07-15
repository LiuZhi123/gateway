package com.digital.hangzhou.gateway.web.config;

import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.web.core.RedisRouteDefinitionRepository;
import com.digital.hangzhou.gateway.web.listener.AddRouteListener;
import com.digital.hangzhou.gateway.web.listener.RefreshSentinelRulesListener;
import com.digital.hangzhou.gateway.web.predicate.ConsumerRoutePredicateFactory;
import com.digital.hangzhou.gateway.web.predicate.WhiteIpRoutePredicateFactory;
import com.digital.hangzhou.gateway.web.listener.DelRouteListener;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;


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


    //redis消息监听器
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory){
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        //添加指定的消息监听器和监听键
        redisMessageListenerContainer.addMessageListener(addRouteListener, new ChannelTopic(RedisConstant.ADD_ROUTES_CHANNEL));
        redisMessageListenerContainer.addMessageListener(delRouteListener, new ChannelTopic(RedisConstant.DELETE_ROUTES_CHANNEL));
        redisMessageListenerContainer.addMessageListener(refreshSentinelRulesListener, new ChannelTopic(RedisConstant.REFRESH_SENTINEL_CHANNEL));

        //监听器需要额外的序列化方法
        Jackson2JsonRedisSerializer seria = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        seria.setObjectMapper(objectMapper);
        redisMessageListenerContainer.setTopicSerializer(seria);

        //配置自定义线程池处理消息
        redisMessageListenerContainer.setTaskExecutor(gatewayExecutor);
        return  redisMessageListenerContainer;
    }
}
