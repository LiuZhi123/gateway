package com.digital.hangzhou.gateway.web.controller;


import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@RestController
public class TestController {
    @Resource
    private RedisTemplate redisTemplate;

    @RequestMapping("/test1")
    public String test1(){
        redisTemplate.opsForValue().set("refreshRoute",1);
        return "test1";
    }

    @RequestMapping("/test2")
    public String test2(){
        redisTemplate.opsForValue().set("refreshSentinel",1);
        return "test2";
    }
}
