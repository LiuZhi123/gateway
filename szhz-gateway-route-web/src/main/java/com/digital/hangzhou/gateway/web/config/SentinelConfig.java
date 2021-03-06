package com.digital.hangzhou.gateway.web.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;

import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

//sentinel?????????
@Slf4j
@Configuration
public class SentinelConfig {

    @Resource
    private RedisTemplate redisTemplate;

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public SentinelConfig(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    //???????????????????????????
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    //?????????????????????
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    @Bean
    public Set<GatewayFlowRule> gatewayFlowRules(){
        return Collections.synchronizedSet(GatewayRuleManager.getRules());
    }


    @PostConstruct
    public void doInit(){
        // ????????????????????????
        initGatewayRules();
        // ????????????????????????????????????
        initBlockHandler();
    }

    /**
     * ??????????????????
     * ??????????????? Sentinel ??????????????????
     */
    private void initGatewayRules() {
        // ?????????????????????????????????
        Map<String, GatewayFlowRule> gatewayRuleCache = redisTemplate.opsForHash().entries(RedisConstant.SENTINEL_RULES);
        Set<GatewayFlowRule> rules = gatewayRuleCache.values().stream().collect(Collectors.toSet());
        GatewayRuleManager.loadRules(rules);
        Object o = redisTemplate.opsForValue().get(RedisConstant.SYSTEM_RULES);
        // ????????????????????????
        List<SystemRule> systemRuleCache = null == o ? new ArrayList<>(0) : (List<SystemRule>) o;
        log.info("<-------????????????????????????????????? {} ???-------->", rules.size() + systemRuleCache.size());
    }

    /**
     * ??????????????????????????????
     */
    private void initBlockHandler() {
        BlockRequestHandler blockRequestHandler = new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
                Map<String, String> result = new HashMap<>(2);
                result.put("code", String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()));
                result.put("message", HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase());
                return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        //.body(BodyInserters.fromValue(result));
                        .body(BodyInserters.fromObject(result));
            }
        };
        // ????????????????????????????????????
        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
    }


}
