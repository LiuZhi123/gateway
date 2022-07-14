package com.digital.hangzhou.gateway.web.controller;



import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RestController
public class TestController {
    @Resource
    private RedisTemplate redisTemplate;

    @RequestMapping("/test1")
    public String test1(){
        redisTemplate.convertAndSend("refreshRoute","apiCode1");
        return "test1";
    }

    @RequestMapping("/test2")
    public String test2(){
        redisTemplate.convertAndSend("refreshSentinel","apiCode");
        return "test2";
    }
}
