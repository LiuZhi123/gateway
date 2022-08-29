package com.digital.hangzhou.gateway.web.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.custom.starters.customwebspringbootstarters.util.Assert;
import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import com.digital.hangzhou.gateway.web.exception.ErrorHandler;
import com.digital.hangzhou.gateway.web.exception.ErrorMessage;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
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
@Slf4j
@Order(2)
@Component
public class ConsumerGatewayFilterFactory extends AbstractGatewayFilterFactory<ConsumerGatewayFilterFactory.Config> {

    public ConsumerGatewayFilterFactory(){
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            Route route = (Route) exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            ServerHttpResponse response = exchange.getResponse();
            ServerHttpRequest request = exchange.getRequest();
            String url = request.getURI().toString();
            MultiValueMap param = request.getQueryParams();
            //apikey标识，适配多种形式
            Integer index = StrUtil.indexOfIgnoreCase(url,RouteInfoConstant.API_KEY);
            String appCode = null;
            if (index >= 0){
                appCode = url.split(url.substring(index, index + 7))[1].split("&")[0];
            }
            if (StrUtil.isBlank(appCode)){
                if (null != exchange.getRequest().getHeaders().get(RouteInfoConstant.API_KEY)){
                    appCode = exchange.getRequest().getHeaders().get(RouteInfoConstant.API_KEY).get(0);
                }
                else if (null != exchange.getRequest().getHeaders().get("apikey")){
                    appCode = exchange.getRequest().getHeaders().get("apikey").get(0);
                }
            }
            if (StrUtil.isBlank(appCode) && CollUtil.isNotEmpty(param)){
                if (param.containsKey("apikey")){
                    appCode = param.get("apikey").toString();
                }
                else if (param.containsKey("apiKey")){
                    appCode = param.get("apiKey").toString();
                }
            }
            if (StrUtil.isNotBlank(appCode) && (StrUtil.contains(appCode,route.getId()))){
                String replace = route.getId() + "_";
                appCode = appCode.replace(replace, "");
            }
            if (StrUtil.isBlank(appCode)){
                return ErrorHandler.writeFailedToResponse(response,ErrorMessage.HTTP_ERROR_401);
            }
            if (!config.sources.contains(appCode)){
                log.info("此次访问的消费者为：" + appCode + " ，路由中的配置消费者为：" + config.sources);
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
