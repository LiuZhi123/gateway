package com.digital.hangzhou.gateway.web.listener;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class RefreshSentinelRulesListener implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        System.out.println("限流规则刷新事件");
    }
}
