package com.digital.hangzhou.gateway.web.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.digital.hangzhou.gateway.client.MetricCollectClient;
import com.digital.hangzhou.gateway.common.constant.CommonConstant;
import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import com.digital.hangzhou.gateway.common.request.RecordRequest;
import com.digital.hangzhou.gateway.web.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author lz
 * @date 2022/11/28 17:42
 */
@Slf4j
@Component
public class MonitorRecordFilter implements GlobalFilter, Ordered {

    @Resource
    private MetricCollectClient metricCollectClient;

    private String[] resources = new String[]{"/css","/js","/png","/jpg","/image","/jsp","/static","/img",
            ".css",".js",".png",".image",".jsp",".static",".img","/grassRoot/"};

    private String[] ignoreRoutes = new String[]{"/grassRoots/*","/grassRoots"};

    private String host = "Host";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //记录请求开始时间
        LocalDateTime startTime = LocalDateTime.now();
        Route route = (Route) exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        ServerHttpRequest request = exchange.getRequest();
        String url = request.getURI().toString();
        MultiValueMap params = request.getQueryParams();
        HttpHeaders headers = exchange.getRequest().getHeaders();
        //针对界面转发需要做特殊处理，过滤静态资源的转发不进行日志记录
        if (request.getPath().toString().startsWith("/HTML_") && StrUtil.containsAnyIgnoreCase(url, resources))
            return chain.filter(exchange);
        //某些路由不需要进行路由转发的日志记录
        if (StrUtil.equalsAny(route.getId(),ignoreRoutes))
            return chain.filter(exchange);
        String orgCode = null;
        if (null != route.getMetadata().get(RouteInfoConstant.ORG_CODE))
            orgCode = route.getMetadata().get(RouteInfoConstant.ORG_CODE).toString();
        String appCode = null;
        if (StrUtil.containsIgnoreCase(url, RouteInfoConstant.API_KEY)){
            Integer index = StrUtil.indexOfIgnoreCase(url,RouteInfoConstant.API_KEY);
            appCode = url.split(url.substring(index, index + 7))[1].split("&")[0];
        }
        if (null == appCode && CollUtil.isNotEmpty(params)){
            if (params.containsKey("apikey"))
                appCode = params.get("apikey").toString();
            else if (params.containsKey("apiKey"))
                appCode = params.get("apiKey").toString();
        }
        if (null == appCode){
            if (null != headers.get(RouteInfoConstant.API_KEY))
                appCode = headers.get(RouteInfoConstant.API_KEY).get(0);
            else if (null != headers.get("apikey"))
                appCode =headers.get("apikey").get(0);
        }
        if (StrUtil.isNotBlank(appCode) && (StrUtil.contains(appCode,route.getId()))){
            String replace = route.getId() + "_";
            appCode = appCode.replace(replace, "");
        }
        String routeId = route.getId();
        //记录数据时，针对于衢州手动增加的数据，无对应的ApiCode,记录时使用name
        if (!StrUtil.startWith(routeId,"API_") && !StrUtil.startWith(routeId, "HTML_")){
            if (CollUtil.isNotEmpty(route.getMetadata()) && null != route.getMetadata().get("name"))
                routeId = route.getMetadata().get("name").toString();
        }
        String remoteIp = IpUtil.getRemoteIp(request);
        String remoteHost = getRemoteHost(request);
        String adCode = null;
        if (StrUtil.contains(url,"adCode=")){
            //防止出现类似于”&adCode=“的情况导致数据越界
            if (url.split("adCode=").length > 1){
                adCode = url.split("adCode=")[1].split("&")[0];
                if (adCode.length() > 12)
                    log.info("异常adCode访问路径:" + url);
            }
        }
        adCode = StrUtil.isNotBlank(adCode) ? adCode : null;
        JSONObject queryParams  = new JSONObject();
        if (CollUtil.isNotEmpty(params))
            queryParams.putAll(params);
        String appKey = headers.containsKey(CommonConstant.APP_KEY) ? headers.get(CommonConstant.APP_KEY).get(0) : null;
        String param = JSON.toJSONString(queryParams);
        RecordRequest recordRequest = new RecordRequest(appCode, orgCode, remoteIp, remoteHost,"200",
                routeId, adCode, param,"0","0",null, appKey);
        return chain.filter(exchange.mutate().response(recordResponseLog(exchange, recordRequest)).build()).then(
                Mono.fromRunnable(()->{
                    ServerHttpResponse response = exchange.getResponse();
                    LocalDateTime endTime = LocalDateTime.now();
                    recordRequest.setResponseTime(String.valueOf(Duration.between(startTime,endTime).toMillis()));
                    Integer status = response.getStatusCode().value();
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
        );
    }

    @Override
    public int getOrder() {
        return -2;
    }

    /**
     * 记录响应日志
     */
    private ServerHttpResponseDecorator recordResponseLog(ServerWebExchange exchange, RecordRequest recordRequest){
        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory bufferFactory = response.bufferFactory();
        MediaType mediaType = exchange.getRequest().getHeaders().getContentType();
        ServerHttpResponseDecorator decoratorResponse = new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if(body instanceof Flux){
                    if(body instanceof Flux){
                        // 过滤上传附件请求
                        if((mediaType != null && mediaType.equals(MediaType.MULTIPART_FORM_DATA)) || (mediaType != null && mediaType.equals(MediaType.APPLICATION_FORM_URLENCODED))){
                            return super.writeWith(body);
                        }

                        Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                        return super.writeWith(fluxBody.map(dataBuffer -> {
                            byte[] content =  new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(content);
                            DataBufferUtils.release(dataBuffer);
                            recordRequest.setResponseSize(String.valueOf(content.length));
                            recordRequest.setResponseResult(new String(content, Charset.forName("utf-8")));
                            return bufferFactory.wrap(content);
                        }));
                    }
                }
                // if body is not a flux. never got there.
                return super.writeWith(body);
            }
        };
        return decoratorResponse;
    }

    private String getRemoteHost(ServerHttpRequest request){
        String result =  request.getRemoteAddress().getHostName();
        if (request.getHeaders().containsKey(host)){
            result = request.getHeaders().get(host).get(0);
        }
        return result;
    }
}
