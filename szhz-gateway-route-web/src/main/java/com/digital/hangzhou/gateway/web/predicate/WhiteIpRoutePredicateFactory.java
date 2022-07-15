package com.digital.hangzhou.gateway.web.predicate;

import cn.hutool.core.collection.CollUtil;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

//IP白名单断言工厂
@Component
public class WhiteIpRoutePredicateFactory extends AbstractRoutePredicateFactory<WhiteIpRoutePredicateFactory.Config> {
    @Resource
    private RedisTemplate redisTemplate;

    public WhiteIpRoutePredicateFactory(){
        super(Config.class);
    }


    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return new Predicate<ServerWebExchange>() {
            @Override
            public boolean test(ServerWebExchange exchange) {
                String remoteIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
                MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();
                List<String> appCode = params.get(RouteInfoConstant.APP_CODE);
                if (CollUtil.isEmpty(appCode)){
                    appCode = exchange.getRequest().getHeaders().get(RouteInfoConstant.APP_CODE);
                }
                if (CollUtil.isEmpty(appCode)){
                    return false;
                }
                //todo IP白名单考虑多网卡情况
                Set<String> whiteIpList =  (Set<String>) redisTemplate.opsForHash().get(RedisConstant.CONSUMER_KEY, appCode);
                if (!whiteIpList.contains(remoteIp)){
                    return false;
                }
                return true;
            }
        };
    }

    public static class Config{
        private String name;

        public String getName(){
            return name;
        }

        public void setName(String name){
            this.name = name;
        }
    }
}
