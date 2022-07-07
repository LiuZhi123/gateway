package com.digital.hangzhou.gateway.web.controller;


import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RequestMapping("/testHello")
    @SentinelResource("test")
    public String testMethod(){
        return "test";
    }
}
