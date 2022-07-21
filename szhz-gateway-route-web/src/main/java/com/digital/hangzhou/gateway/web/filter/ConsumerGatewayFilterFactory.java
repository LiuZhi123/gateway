package com.digital.hangzhou.gateway.web.filter;

import cn.hutool.core.collection.CollUtil;
import com.custom.starters.customwebspringbootstarters.util.Assert;
import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import com.digital.hangzhou.gateway.web.exception.ErrorHandler;
import com.digital.hangzhou.gateway.web.exception.ErrorMessage;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 消费者过滤器
 */
@Order(2)
@Component
public class ConsumerGatewayFilterFactory extends AbstractGatewayFilterFactory<ConsumerGatewayFilterFactory.Config> {

    public ConsumerGatewayFilterFactory(){
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpResponse response =exchange.getResponse();
            MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();
            List<String> appCode = params.get(RouteInfoConstant.API_KEY);
            if (CollUtil.isEmpty(appCode)){
                appCode = exchange.getRequest().getHeaders().get(RouteInfoConstant.API_KEY);
            }
            if (null == appCode || CollUtil.isEmpty(appCode)){
                return ErrorHandler.writeFailedToResponse(response,ErrorMessage.HTTP_ERROR_401);
            }
            if (!config.sources.contains(appCode.get(0))){
                return ErrorHandler.writeFailedToResponse(response,ErrorMessage.HTTP_ERROR_402);
            }
            return chain.filter(exchange);
        });
    }


    public static class Config {

        private List<String> sources = new ArrayList<>();

        public List<String> getSources() {
            return sources;
        }

        public Config setSources(List<String> sources) {
            this.sources = sources;
            return this;
        }

        public Config setSources(String... sources) {
            this.sources = Arrays.asList(sources);
            return this;
        }
    }

}
