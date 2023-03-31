package com.digital.hangzhou.gateway.web.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.custom.starters.customwebspringbootstarters.core.exceptions.IErrorCode;
import com.custom.starters.customwebspringbootstarters.util.Assert;
import com.digital.hangzhou.gateway.common.constant.ApiConstant;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import com.digital.hangzhou.gateway.common.domain.dto.GlobalConfig;
import com.digital.hangzhou.gateway.web.exception.ErrorHandler;
import com.digital.hangzhou.gateway.web.exception.ErrorMessage;
import com.digital.hangzhou.gateway.web.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 白名单过滤器
 */
@Order(3)
@Component
@Slf4j
public class WhiteIpGatewayFilterFactory extends AbstractGatewayFilterFactory {
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public GatewayFilter apply(Object config) {
        return ((exchange, chain) -> {
            //判断全局白名单是否开启，开启则不走路由的白名单过滤器
            if (null != redisTemplate.opsForValue().get(RedisConstant.GATEWAY_GLOBAL_CONFIG)){
                GlobalConfig globalConfig = JSONObject.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(RedisConstant.GATEWAY_GLOBAL_CONFIG)), GlobalConfig.class);
                if (null != globalConfig && globalConfig.getWhiteIpStatus() == true){
                    return chain.filter(exchange);
                }
            }
            Route route = (Route) exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            ServerHttpResponse response = exchange.getResponse();
            ServerHttpRequest request = exchange.getRequest();
            log.info("IP白名单请求头: "  + request.getHeaders());
            log.info("request HostName: " + request.getRemoteAddress().getHostName());
            log.info("request HostAddress: " + request.getRemoteAddress().getAddress().getHostAddress());
            String remoteIp = IpUtil.getRemoteIp(request);
            String url = request.getURI().toString();
            //apikey标识，适配多种形式
            Integer index = StrUtil.indexOfIgnoreCase(url,RouteInfoConstant.API_KEY);
            String apiKey = null;
            if (index >= 0){
                apiKey = url.split(url.substring(index, index + 7))[1].split("&")[0];
            }
            if (StrUtil.isBlank(apiKey)){
                apiKey = CollUtil.isEmpty(exchange.getRequest().getHeaders().get(index)) ? null : exchange.getRequest().getHeaders().get(index).get(0);
            }
            if (StrUtil.isNotBlank(apiKey) && (StrUtil.contains(apiKey,route.getId()))){
                String replace = route.getId() + "_";
                apiKey = apiKey.replace(replace, "");
            }
            if (StrUtil.isNotBlank(apiKey)){
                //todo IP白名单考虑多网卡情况
                Set<String> whiteIpList =  (Set<String>) redisTemplate.opsForHash().get(route.getId(), apiKey);
                if (null == whiteIpList || !whiteIpList.contains(remoteIp)){
                    return ErrorHandler.writeFailedToResponse(response, ErrorMessage.HTTP_ERROR_403);
                }
            }else {
                Set<String> apiKeys = redisTemplate.opsForHash().keys(route.getId());
                if (null != apiKeys && apiKeys.size() > 0){
                     List<Set<String>> ipSet =  redisTemplate.opsForHash().multiGet(route.getId(), apiKeys);
                     Set<String> ips = new HashSet<>();
                     ipSet.stream().forEach(e->{
                         ips.addAll(e);
                     });
                     if (!CollUtil.contains(ips, remoteIp)){
                         return ErrorHandler.writeFailedToResponse(response, ErrorMessage.HTTP_ERROR_403);
                     }
                }
                else {
                    return ErrorHandler.writeFailedToResponse(response, ErrorMessage.HTTP_ERROR_403);
                }
            }
            return chain.filter(exchange);
        });
    }
}
