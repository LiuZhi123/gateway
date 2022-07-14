package com.digital.hangzhou.gateway.web.listener;


import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;


@Component
public class RefreshRouteListener implements MessageListener {
    //键事件处理业务逻辑
    @Override
    public void onMessage(Message message, byte[] bytes) {
        System.out.println("监听事件========");
    }


}
