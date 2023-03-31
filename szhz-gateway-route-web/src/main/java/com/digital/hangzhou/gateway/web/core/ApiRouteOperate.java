package com.digital.hangzhou.gateway.web.core;

import com.digital.hangzhou.gateway.common.enums.ReleaseStatusEnum;
import com.digital.hangzhou.gateway.common.request.ReleaseRequest;
import com.digital.hangzhou.gateway.web.cache.LocalCacheRepository;
import com.digital.hangzhou.gateway.web.event.RefreshRouteEvent;
import com.digital.hangzhou.gateway.web.util.RouteDefinitionUtil;
import com.digital.hangzhou.gateway.web.util.SentinelRuleUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class ApiRouteOperate implements RouteOperate{
    @Resource
    private RefreshRouteEvent refreshRouteEvent;
    @Resource
    private SentinelRuleUtil sentinelRuleUtil;
    @Resource
    private RouteDefinitionUtil routeDefinitionUtil;

    @Override
    public void save(ReleaseRequest request) {
        log.info("收到" + request.getReleaseStatus() + "路由"+ request.getApiCode() + "请求:" + request);
        if (request.getReleaseStatus() == ReleaseStatusEnum.OFFLINE){
            delete(request.getApiCode());
            return;
        }
        //根据Api编号查询路由的消费者信息以及IP白名单信息模板信息以及限流信息生成对应的routeDefinition对象
        RouteDefinition routeDefinition = routeDefinitionUtil.getApiRouteDefinition(request);
        //保存路由信息至内存
        refreshRouteEvent.saveAndNotify(routeDefinition);
        sentinelRuleUtil.addGatewaySentinelRule(request.getApiCode(), request.getConfig());
    }

    @Override
    public void delete(String routeId) {
        //根据路由ID删除路由
        if (!LocalCacheRepository.ROUTE_DEFINITION_CACHE.containsKey(routeId)){
            return;
        }
        refreshRouteEvent.deleteAndNotify(routeId);
        sentinelRuleUtil.delGatewaySentinelRule(routeId);
    }
}
