package com.digital.hangzhou.gateway.web.core;

import cn.hutool.core.util.StrUtil;
import com.digital.hangzhou.gateway.common.enums.ApiAuthType;
import com.digital.hangzhou.gateway.common.enums.HtmlAccessType;
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
    @Resource
    private RouteDefinitionUtil routeDefinitionUtil;

    @Override
    public void save(ReleaseRequest request) {
        if (request.getReleaseStatus() == ReleaseStatusEnum.OFFLINE){
            delete(request.getHtmlInstanceCode());
            delete(request.getHtmlInstanceCode() + "_Auth");
            return;
        }
        RouteDefinition html = routeDefinitionUtil.getHtmlRouteDefinition(request);
        refreshRouteEvent.saveAndNotify(html);
        sentinelRuleUtil.addGatewaySentinelRule(request.getHtmlInstanceCode(), request.getConfig());
        //非重定向的界面不需要生成js路由
        if (StrUtil.isNotBlank(request.getHtmlAccessType()) && StrUtil.equals(HtmlAccessType.STANDARD.name(), request.getHtmlAccessType())){
            //界面路由需要生成两个html和js路由，其中html路由增加监控，鉴权等过滤器配置
            if (!request.getAuthType().equals(ApiAuthType.PUBLIC)){
                RouteDefinition htmlAuth = routeDefinitionUtil.getHtmlAuthRouteDefinition(request);
                refreshRouteEvent.saveAndNotify(htmlAuth);
            }
            //js请求的路由可能会重复，不考虑刷新
            if (LocalCacheRepository.ROUTE_DEFINITION_CACHE.containsKey(request.getHtmlPredicatePath())){
                return;
            }
            else {
                RouteDefinition js = routeDefinitionUtil.getJsRouteDefinition(request);
                refreshRouteEvent.saveAndNotify(js);
            }
        }
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
