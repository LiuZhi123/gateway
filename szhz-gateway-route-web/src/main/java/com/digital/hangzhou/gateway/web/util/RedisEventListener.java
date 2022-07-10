package com.digital.hangzhou.gateway.web.util;


import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;


@Component
public class RedisEventListener  implements MessageListener, ApplicationEventPublisherAware {


    private ApplicationEventPublisher publisher;


    //键事件处理业务逻辑
    @Override
    public void onMessage(Message message, byte[] bytes) {
        System.out.println("监听事件========");
    }




    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    protected void publishEvent(ApplicationEvent event) {
        if (publisher != null) {
            this.publisher.publishEvent(event);
        }
    }


}
