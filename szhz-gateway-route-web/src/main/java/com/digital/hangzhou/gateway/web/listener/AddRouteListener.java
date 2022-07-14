package com.digital.hangzhou.gateway.web.listener;

import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.web.cache.LocalCacheRepository;
import com.digital.hangzhou.gateway.web.event.RefreshRouteEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class AddRouteListener implements MessageListener {
    @Resource
    private RefreshRouteEvent refreshRouteEvent;
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("监听到增加路由事件: " + message.toString());
        if (LocalCacheRepository.ROUTE_DEFINITION_CACHE.containsKey(message.toString())){
            return;
        }
        RouteDefinition routeDefinition =  (RouteDefinition) redisTemplate.opsForHash().get(RedisConstant.ROUTE_KEY, message.toString());
        if (null != routeDefinition){
            refreshRouteEvent.save(routeDefinition);
        }
    }
}
