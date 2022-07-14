package com.digital.hangzhou.gateway.web.listener;


import com.digital.hangzhou.gateway.web.cache.LocalCacheRepository;
import com.digital.hangzhou.gateway.web.event.RefreshRouteEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DelRouteListener implements MessageListener {
    @Resource
    private RefreshRouteEvent refreshRouteEvent;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        log.info("监听到删除路由事件：" + message.toString());
        //本地缓存中存在key则删除
        if (LocalCacheRepository.ROUTE_DEFINITION_CACHE.containsKey(message.toString())){
            refreshRouteEvent.delete(message.toString());
        }
    }
}
