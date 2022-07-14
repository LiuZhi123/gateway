package com.digital.hangzhou.gateway.web.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharPool;
import com.custom.starters.customwebspringbootstarters.core.result.R;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import com.digital.hangzhou.gateway.common.request.ReleaseRequest;
import lombok.SneakyThrows;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

public class RouteDefinitionUtil {
    /**
     * 根据request请求生成routeDefinition对象
     * @return
     */
    @SneakyThrows
    public static RouteDefinition getApiRouteDefinition(ReleaseRequest request){
        RouteDefinition routeDefinition = new RouteDefinition();
        //API的转发路径为API的全路径
        String url = request.getServiceProtocol().name() + CharPool.COLON + CharPool.SLASH + CharPool.SLASH
                + request.getServiceAddress() + CharPool.COLON + request.getServicePort() + request.getFullPath();
        //ID为API编号
        routeDefinition.setId(request.getApiCode());
        routeDefinition.setUri(new URI(url));
        //元数据存储API所属部门，用于监控数据统计
        Map<String, Object> metaData = new HashMap(1){
            {
                put(RouteInfoConstant.ORG_CODE, request.getMainOrgCode());
            }
        };
        routeDefinition.setMetadata(metaData);
        //断言工厂
        routeDefinition.setPredicates(getPredicateList(request));
        //过滤器链
        routeDefinition.setFilters(getFilterDefinition(request));
        return  routeDefinition;
    }


    public static List<PredicateDefinition> getPredicateList(ReleaseRequest request){
        List<PredicateDefinition> predicateDefinitions = new ArrayList<>();
        PredicateDefinition path = new PredicateDefinition();
        path.setName(RouteInfoConstant.PATH_PREDICATE_FACTORY);
        path.addArg("patterns", CharPool.SLASH + request.getApiCode() + CharPool.SLASH + "**");
        predicateDefinitions.add(path);
        switch (request.getAuthType()){
            case PUBLIC:
                break;
            case DISABLE:
                PredicateDefinition consumer = new PredicateDefinition();
                consumer.setName(RouteInfoConstant.CONSUMER_PREDICATE_FACTORY);
                consumer.setArgs(new HashMap(){{
                    put("consumers", request.getConsumerList());
                }});
                predicateDefinitions.add(consumer);
            case AUTHOR:
                PredicateDefinition author = new PredicateDefinition();
                author.setName(RouteInfoConstant.CONSUMER_PREDICATE_FACTORY);
                author.setArgs(new HashMap(){{
                    put("consumers", request.getConsumerList());
                }});
                predicateDefinitions.add(author);
                //白名单断言工厂
                PredicateDefinition whiteIpList = new PredicateDefinition();
                whiteIpList.setName(RouteInfoConstant.WHITE_IP_PREDICATE_FACTORY);
                predicateDefinitions.add(whiteIpList);
        }
        return predicateDefinitions;
    }


    public static List<FilterDefinition> getFilterDefinition(ReleaseRequest request){
        List<FilterDefinition> filterDefinitionList = new ArrayList<>();
        //去除url中的参数过滤器，即系统中自定义的ApiCode
        FilterDefinition stripPrefix = new FilterDefinition();
        stripPrefix.setName(RouteInfoConstant.STRIP_PREFIX_GATEWAY_FILTER);
        stripPrefix.addArg("parts","1");
        //如果有配置鉴权模板，那么在转发下游之前增加请求头参数
        if (CollUtil.isNotEmpty(request.getAuthTemplate())){
            Set<String> keys = request.getAuthTemplate().keySet();
            for (String  key : keys){
                FilterDefinition addRequestHeader = new FilterDefinition();
                addRequestHeader.setName(RouteInfoConstant.ADD_REQUEST_HEADER_GATEWAY_FILTER);
                addRequestHeader.setArgs(request.getAuthTemplate());
            }
        }
        filterDefinitionList.add(stripPrefix);
        return filterDefinitionList;
    }
}
