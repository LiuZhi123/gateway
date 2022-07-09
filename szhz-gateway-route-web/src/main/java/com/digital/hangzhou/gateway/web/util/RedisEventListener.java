package com.digital.hangzhou.gateway.web.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyspaceEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.Set;

public class RedisEventListener extends KeyspaceEventMessageListener implements ApplicationEventPublisherAware {

    private static String KEYEVENT_EXPIRED_TOPIC = "__keyevent@0__:expired";

    private static String KEYEVENT_SET_TOPIC = "__keyevent@0__:set";

    private ApplicationEventPublisher publisher;

    //监听指定库
    @Value("${spring.redis.datasource}")
    private int dataBase;

//    @Value("${}")
    //监听指定键
    private Set<String> keySet;

    public RedisEventListener(RedisMessageListenerContainer listenerContainer){
        super(listenerContainer);
    }

    public void setDataBase(int dataBase) {
        this.dataBase = dataBase;
        setTopic();
    }

    public Set<String> getKeySet() {
        return keySet;
    }

    public void setKeySet(Set<String> keySet) {
        this.keySet = keySet;
    }

    private void setTopic() {
        KEYEVENT_EXPIRED_TOPIC = "__keyevent@" + dataBase + "__:expired";
        KEYEVENT_SET_TOPIC = "__keyevent@" + dataBase + "__:set";
    }

    //业务处理逻辑
    @Override
    protected void doHandleMessage(Message message) {

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
