package com.digital.hangzhou.gateway.web.core;

import com.digital.hangzhou.gateway.common.enums.ReleaseStatusEnum;
import com.digital.hangzhou.gateway.common.request.ReleaseRequest;
import com.digital.hangzhou.gateway.web.cache.LocalCacheRepository;
import com.digital.hangzhou.gateway.web.event.RefreshRouteEvent;
import com.digital.hangzhou.gateway.web.util.RouteDefinitionUtil;
import com.digital.hangzhou.gateway.web.util.SentinelRuleUtil;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class HtmlRouteOperate implements RouteOperate{
    @Resource
    private RefreshRouteEvent refreshRouteEvent;
    @Resource
    private SentinelRuleUtil sentinelRuleUtil;

    @Override
    public void save(ReleaseRequest request) {
        if (request.getReleaseStatus() == ReleaseStatusEnum.OFFLINE){
            delete(request.getApiCode());
            return;
        }
        //界面路由需要生成两个html和js路由，其中html路由增加监控，鉴权等过滤器配置
        RouteDefinition html = RouteDefinitionUtil.getHtmlRouteDefinition(request);
        refreshRouteEvent.saveAndNotify(html);
        sentinelRuleUtil.addGatewaySentinelRule(request.getHtmlInstanceCode(), request.getConfig());
        //js路由可能会重复
        if (LocalCacheRepository.ROUTE_DEFINITION_CACHE.containsKey(request.getHtmlPredicatePath())){
            return;
        }
        RouteDefinition js = RouteDefinitionUtil.getJsRouteDefinition(request);
        refreshRouteEvent.saveAndNotify(js);
    }

    @Override
    public void delete(String routeId) {
        if (!LocalCacheRepository.ROUTE_DEFINITION_CACHE.containsKey(routeId)){
            return;
        }
        refreshRouteEvent.deleteAndNotify(routeId);
        sentinelRuleUtil.delGatewaySentinelRule(routeId);
    }
}
