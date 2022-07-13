package com.digital.hangzhou.gateway.web.predicate;

import cn.hutool.core.collection.CollUtil;
import com.custom.starters.customwebspringbootstarters.util.Assert;
import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

//消费者断言工厂
public class ConsumerPredicateFactory extends AbstractRoutePredicateFactory<ConsumerPredicateFactory.Config> {

    private static  final  String CONSUMERS_KEY = "consumers";

    public ConsumerPredicateFactory(){
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(CONSUMERS_KEY);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return new Predicate<ServerWebExchange>() {
            @Override
            public boolean test(ServerWebExchange exchange) {
                MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();
                List<String> appCode = params.get(RouteInfoConstant.API_KEY);
                if (CollUtil.isEmpty(appCode)){
                    appCode = exchange.getRequest().getHeaders().get(RouteInfoConstant.API_KEY);
                }
                if (CollUtil.isEmpty(appCode) || !config.consumerCodes.contains(appCode.get(0))){
                    return false;
                }
                return true;
            }
        };
    }


    //可放行的消费者列表
    public static class Config{
        private Set<String> consumerCodes;

        public Set<String> getConsumerCodes() {
            return consumerCodes;
        }

        public void setConsumerCodes(Set<String> consumerCodes) {
            this.consumerCodes = consumerCodes;
        }
    }
}
