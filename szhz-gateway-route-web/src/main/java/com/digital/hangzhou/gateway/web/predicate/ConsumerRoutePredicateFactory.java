//package com.digital.hangzhou.gateway.web.predicate;
//
//import cn.hutool.core.collection.CollUtil;
//import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
//import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
//import org.springframework.stereotype.Component;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.server.ServerWebExchange;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Set;
//import java.util.function.Predicate;
//
////消费者断言工厂
//@Component
//public class ConsumerRoutePredicateFactory extends AbstractRoutePredicateFactory<ConsumerRoutePredicateFactory.Config> {
//
//    private static final  String CONSUMERS_KEY = "consumers";
//
//    public ConsumerRoutePredicateFactory(){
//        super(Config.class);
//    }
//
//    @Override
//    public List<String> shortcutFieldOrder() {
//        return Arrays.asList(CONSUMERS_KEY);
//    }
//
//    @Override
//    public ShortcutType shortcutType() {
//        return ShortcutType.GATHER_LIST;
//    }
//
//    @Override
//    public Predicate<ServerWebExchange> apply(Config config) {
//        return new Predicate<ServerWebExchange>() {
//            @Override
//            public boolean test(ServerWebExchange exchange) {
//                MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();
//                List<String> appCode = params.get(RouteInfoConstant.API_KEY);
//                if (CollUtil.isEmpty(appCode)){
//                    appCode = exchange.getRequest().getHeaders().get(RouteInfoConstant.API_KEY);
//                }
//                if (CollUtil.isEmpty(appCode) || !config.getConsumers().contains(appCode.get(0))){
//                    return false;
//                }
//                return true;
//            }
//        };
//    }
//
//
//    //可放行的消费者列表
//    public static class Config{
//        private List<String> consumers = new ArrayList<>();
//
//        public List<String> getConsumers() {
//            return consumers;
//        }
//
//        public void setConsumers(List<String> consumerCodes) {
//            this.consumers = consumerCodes;
//        }
//    }
//}
