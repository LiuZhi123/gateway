package com.digital.hangzhou.gateway.web.filter;

import cn.hutool.core.collection.CollUtil;
import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import com.digital.hangzhou.gateway.common.request.RecordRequest;
import com.digital.hangzhou.gateway.web.client.MetricCollectClient;
import com.digital.hangzhou.gateway.web.util.CustomizeMetric;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Order(1)
//网关转发监控日志记录
@Component
public class MonitorGatewayFilterFactory extends AbstractGatewayFilterFactory {
    @Resource
    private MetricCollectClient metricCollectClient;

    @Override
    public GatewayFilter apply(Object config) {
        return ((exchange, chain) -> {
            Route route = (Route) exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            ServerHttpRequest request = exchange.getRequest();
            String orgCode = route.getMetadata().get(RouteInfoConstant.ORG_CODE).toString();
            MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();
            List<String> appCodeList = params.get(RouteInfoConstant.API_KEY);
            if (CollUtil.isEmpty(appCodeList)){
                appCodeList = exchange.getRequest().getHeaders().get(RouteInfoConstant.API_KEY);
            }
            String appCode = null == appCodeList.get(0) ? null : appCodeList.get(0);
            String routeId = route.getId();
            String remoteIp = request.getRemoteAddress().getAddress().getHostAddress();
            String remoteHost = request.getRemoteAddress().getHostName();
            return chain.filter(exchange).then(
                    Mono.fromRunnable(()->{
                        ServerHttpResponse response = exchange.getResponse();
                        Integer status = response.getStatusCode().value();
                        CustomizeMetric.getInstance().counter().labels(orgCode, appCode, remoteIp, remoteHost, routeId, status.toString()).inc();
                        //异步落库
                        Call<Void> call = metricCollectClient.record(new RecordRequest(appCode, orgCode, remoteIp, remoteHost, status.toString(), routeId));
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                            }
                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                log.error("路由日志记录异常：" + t.getMessage());
                            }
                        });
                    })
            );
        });
    }
}
