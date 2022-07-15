package com.digital.hangzhou.gateway.web.service;

import cn.hutool.core.collection.CollUtil;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.common.request.ReleaseRequest;
import com.digital.hangzhou.gateway.web.core.RedisRouteDefinitionRepository;
import com.digital.hangzhou.gateway.web.event.RefreshRouteEvent;
import com.digital.hangzhou.gateway.web.util.RouteDefinitionUtil;
import com.digital.hangzhou.gateway.web.util.SentinelRuleUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.List;

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

    /**
     * 根据请求生成动态路由并加载至内存，发送事件通知所有节点刷新路由
     * @param request
     */
    public void save(ReleaseRequest request){
        //根据Api编号查询路由的消费者信息以及IP白名单信息模板信息以及限流信息生成对应的routeDefinition对象
        RouteDefinition routeDefinition = RouteDefinitionUtil.getApiRouteDefinition(request);
        //保存路由信息至内存
        refreshRouteEvent.saveAndNotify(routeDefinition);
        sentinelRuleUtil.addGatewaySentinelRule(request.getApiCode(), request.getConfig());
    }

    public void delete(String routeId){
        //根据路由ID删除路由
        refreshRouteEvent.deleteAndNotify(routeId);
        sentinelRuleUtil.delGatewaySentinelRule(routeId);
    }
}
