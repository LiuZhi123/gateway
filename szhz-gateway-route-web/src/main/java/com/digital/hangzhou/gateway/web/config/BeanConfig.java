package com.digital.hangzhou.gateway.web.config;

import com.digital.hangzhou.gateway.web.predicate.HtmlResourceRoutePredicateFactory;
import com.digital.hangzhou.gateway.web.component.RedisRouteDefinitionRepository;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class BeanConfig {
//    //自定义的路由增加删除操作，此版本基于redis缓存路由信息，再save与delete前后同步redis中的信息
//    @Bean
//    RouteDefinitionRepository repository(){
//        return new RedisRouteDefinitionRepository();
//    }

    //自定义路由管理器，自定义的断言工厂与过滤器工厂需要在此处添加
    @Bean
    public RouteLocator customRouteLocator(GatewayProperties properties, List<GatewayFilterFactory> gatewayFilterFactories,
                                     List<RoutePredicateFactory> predicateFactories, RouteDefinitionLocator routeDefinitionLocator,
                                     ConfigurationService service){
        //增加自定义断言工厂
        predicateFactories.add(new HtmlResourceRoutePredicateFactory());
        return new RouteDefinitionRouteLocator(routeDefinitionLocator, predicateFactories, gatewayFilterFactories, properties, service);
    }

    //网关涉及的跨域配置
    @Bean
    public CorsWebFilter corsWebFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", corsConfiguration);
        return  new CorsWebFilter(source);
    }
}
