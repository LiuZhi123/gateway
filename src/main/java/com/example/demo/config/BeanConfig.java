package com.example.demo.config;

import com.example.demo.service.impl.RedisRouterDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {
    @Bean
    RouteDefinitionRepository repository(){
        return new RedisRouterDefinitionRepository();
    }
}
