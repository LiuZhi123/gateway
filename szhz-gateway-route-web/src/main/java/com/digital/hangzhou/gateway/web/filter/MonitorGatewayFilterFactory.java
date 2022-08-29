package com.digital.hangzhou.gateway.web.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.digital.hangzhou.gateway.client.MetricCollectClient;
import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import com.digital.hangzhou.gateway.common.request.RecordRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
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
@Order(Ordered.HIGHEST_PRECEDENCE)
//网关转发监控日志记录
@Component
public class MonitorGatewayFilterFactory extends AbstractGatewayFilterFactory {
    private String[] resources = new String[]{"/css","/js","/png","/jpg","/image","/jsp","/static","/img",
            ".css",".js",".png",".image",".jsp",".static",".img","/grassRoot/"};

    private String[] ignoreRoutes = new String[]{"/grassRoots/*","/grassRoots"};

    @Resource
    private MetricCollectClient metricCollectClient;

    @Override
    public GatewayFilter apply(Object config) {
        return ((exchange, chain) -> {
            Route route = (Route) exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            ServerHttpRequest request = exchange.getRequest();
            String url = request.getURI().toString();
            MultiValueMap params = request.getQueryParams();
            //针对界面转发需要做特殊处理，过滤静态资源的转发不进行日志记录
            if (request.getPath().toString().startsWith("/HTML_") && StrUtil.containsAnyIgnoreCase(url, resources)){
               return chain.filter(exchange);
            }
            //某些路由不需要进行路由转发的日志记录
            if (StrUtil.equalsAny(route.getId(),ignoreRoutes)){
                return chain.filter(exchange);
            }
            String orgCode = null;
            if (null != route.getMetadata().get(RouteInfoConstant.ORG_CODE)){
                orgCode = route.getMetadata().get(RouteInfoConstant.ORG_CODE).toString();
            }
            String appCode = null;
            if (StrUtil.containsIgnoreCase(url, RouteInfoConstant.API_KEY)){
                Integer index = StrUtil.indexOfIgnoreCase(url,RouteInfoConstant.API_KEY);
                appCode = url.split(url.substring(index, index + 7))[1].split("&")[0];
            }
            if (null == appCode && CollUtil.isNotEmpty(params)){
                if (params.containsKey("apikey")){
                    appCode = params.get("apikey").toString();
                }
                else if (params.containsKey("apiKey")){
                    appCode = params.get("apiKey").toString();
                }
            }
            if (null == appCode){
                if (null != exchange.getRequest().getHeaders().get(RouteInfoConstant.API_KEY)){
                    appCode = exchange.getRequest().getHeaders().get(RouteInfoConstant.API_KEY).get(0);
                }
                else if (null != exchange.getRequest().getHeaders().get("apikey")){
                    appCode = exchange.getRequest().getHeaders().get("apikey").get(0);
                }
            }
            if (StrUtil.isNotBlank(appCode) && (StrUtil.contains(appCode,route.getId()))){
                String replace = route.getId() + "_";
                appCode = appCode.replace(replace, "");
            }
            String routeId = route.getId();
            //记录数据时，针对于衢州手动增加的数据，无对应的ApiCode,记录时使用name
            if (!StrUtil.startWith(routeId,"API_") && !StrUtil.startWith(routeId, "HTML_")){
                if (CollUtil.isNotEmpty(route.getMetadata()) && null !=  route.getMetadata().get("name")){
                    routeId = route.getMetadata().get("name").toString();
                }
            }
            String remoteIp = request.getRemoteAddress().getAddress().getHostAddress();
            String remoteHost = request.getRemoteAddress().getHostName();
            String adCode = null;
            if (StrUtil.contains(url,"adCode=")){
                adCode = url.split("adCode=")[1].split("&")[0];
                if (adCode.length() > 12){
                    log.info("异常adCode访问路径:" + url);
                }
            }
            adCode = StrUtil.isNotBlank(adCode) ? adCode : null;
            JSONObject queryParams  = new JSONObject();
            if (CollUtil.isNotEmpty(params)){
                queryParams.putAll(params);
            }
            String param = JSON.toJSONString(queryParams);
            RecordRequest recordRequest = new RecordRequest(appCode,orgCode,remoteIp, remoteHost,"200",routeId,adCode,param);
            return chain.filter(exchange)
                            .then(
                    Mono.fromRunnable(()->{
                        ServerHttpResponse response = exchange.getResponse();
                        Integer status = response.getStatusCode().value();
                        //异步落库
                        recordRequest.setStatus(status.toString());
                        Call<Void> call = metricCollectClient.record(recordRequest);
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
            )
                    ;
        });
    }
}
