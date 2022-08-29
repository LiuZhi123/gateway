package com.digital.hangzhou.gateway.web;

import com.custom.starters.customwebspringbootstarters.annotation.EnableExceptionHandler;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@EnableExceptionHandler
@SpringBootApplication(scanBasePackages = "com.digital.hangzhou.gateway")
@RetrofitScan(basePackages = {"com.digital.hangzhou.gateway.client"})
public class GatewayApplication {
    public static void main(String[] args) {

        SpringApplication.run(GatewayApplication.class, args);
    }
}

