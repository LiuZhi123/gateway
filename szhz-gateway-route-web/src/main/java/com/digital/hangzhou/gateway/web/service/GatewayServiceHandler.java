package com.digital.hangzhou.gateway.web.service;

import cn.hutool.core.collection.CollUtil;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import com.digital.hangzhou.gateway.common.request.GlobalRuleRequest;
import com.digital.hangzhou.gateway.common.request.ReleaseAuthRequest;
import com.digital.hangzhou.gateway.web.event.RefreshRouteEvent;
import com.digital.hangzhou.gateway.web.util.RouteDefinitionUtil;
import com.digital.hangzhou.gateway.web.util.SentinelRuleUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GatewayServiceHandler implements CommandLineRunner {
    @Resource
    private RefreshRouteEvent refreshRouteEvent;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private SentinelRuleUtil sentinelRuleUtil;

    //容器启动后从缓存中加载路由信息
    @Override
    public void run(String... args) {
        //从缓存中加载路由信息
        List<RouteDefinition> routeDefinitionList = redisTemplate.opsForHash().values(RedisConstant.ROUTE_KEY);
        log.info("<-------系统初始化从缓存加载路由信息 {} 条-------->", routeDefinitionList.size());
        refreshRouteEvent.saveBatch(routeDefinitionList);
        log.info("<------------初始化路由信息加载完毕------------------>");
    }


    public void refresh(ReleaseAuthRequest request){
        List<RouteDefinition> routeDefinitionList = getRouteByIds(CollUtil.toList(request.getApiInstanceCode()));
        if (CollUtil.isEmpty(routeDefinitionList)){
            return;
        }
        RouteDefinition exist = routeDefinitionList.get(0);
        List<FilterDefinition> filters = exist.getFilters();
        FilterDefinition consumer = new FilterDefinition();
        consumer.setName(RouteInfoConstant.CONSUMER_PREDICATE_FACTORY);
        consumer.addArg("sources" , RouteDefinitionUtil.getConsumer(request.getAppCodes()).toString());
        filters.removeAll(filters.stream().filter(e->RouteInfoConstant.CONSUMER_PREDICATE_FACTORY.equals(e.getName())).collect(Collectors.toList()));
        filters.add(consumer);
        exist.setFilters(filters);
        refreshRouteEvent.saveAndNotify(exist);
    }

    public List<RouteDefinition> getRouteByIds(List<String> ids){
        List<RouteDefinition> routeDefinitionList = redisTemplate.opsForHash().multiGet(RedisConstant.ROUTE_KEY, ids);
        return routeDefinitionList == null ? new ArrayList<>(0) : routeDefinitionList;
    }

    public void saveSystemRules(GlobalRuleRequest globalRuleRequest){
        sentinelRuleUtil.systemRules(globalRuleRequest.getLimitStatus(), Double.valueOf(globalRuleRequest.getLimitRate()));
    }}
