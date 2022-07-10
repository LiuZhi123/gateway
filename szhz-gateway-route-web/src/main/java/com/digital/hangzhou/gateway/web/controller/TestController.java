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

    @RequestMapping("/testHello")
    @SentinelResource("test")



    public String testMethod(){
        redisTemplate.expire("gatewayNotify",1, TimeUnit.MILLISECONDS);
        return "test";
    }
}
