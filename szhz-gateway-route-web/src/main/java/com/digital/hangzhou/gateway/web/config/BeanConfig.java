package com.digital.hangzhou.gateway.web.config;

import com.digital.hangzhou.gateway.web.predicate.HtmlResourceRoutePredicateFactory;
import com.digital.hangzhou.gateway.web.component.RedisRouteDefinitionRepository;
import com.digital.hangzhou.gateway.web.util.RedisEventListener;

import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;


import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Executor;

@Configuration
public class BeanConfig {

    //定义触发redis事件的key操作
    private static String KEYEVENT_SET_TOPIC = "__keyevent@0__:set";

    @Resource
    private RedisEventListener redisEventListener;

    @Resource
    private Executor gatewayExecutor;


//    自定义路由管理器，自定义的断言工厂与过滤器工厂需要在此处添加
    @Bean
    public RouteLocator customRouteLocator(GatewayProperties properties, List<GatewayFilterFactory> gatewayFilterFactories,
                                     List<RoutePredicateFactory> predicateFactories, RedisRouteDefinitionRepository redisRouteDefinitionRepository,
                                     ConfigurationService service){
        //增加自定义断言工厂
        predicateFactories.add(new HtmlResourceRoutePredicateFactory());
        return new RouteDefinitionRouteLocator(redisRouteDefinitionRepository, predicateFactories, gatewayFilterFactories, properties, service);
    }



    @Bean
    public ChannelTopic channelTopic(){
        return new ChannelTopic(KEYEVENT_SET_TOPIC);
    }

    //redis消息监听器
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory){
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        //添加指定的消息监听器和监听键
        redisMessageListenerContainer.addMessageListener(redisEventListener, channelTopic());
        //配置自定义线程池处理消息
        redisMessageListenerContainer.setTaskExecutor(gatewayExecutor);
        return  redisMessageListenerContainer;
    }


}
