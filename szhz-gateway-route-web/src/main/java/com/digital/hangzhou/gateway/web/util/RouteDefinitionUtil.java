package com.digital.hangzhou.gateway.web.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharPool;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import com.digital.hangzhou.gateway.common.enums.ApiAuthType;
import com.digital.hangzhou.gateway.common.request.ReleaseRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;

import java.net.URI;
import java.util.*;

@Slf4j
public class RouteDefinitionUtil {
    /**
     * 根据request请求生成routeDefinition对象
     * @return
     */
    @SneakyThrows
    public static RouteDefinition getApiRouteDefinition(ReleaseRequest request){
        RouteDefinition routeDefinition = new RouteDefinition();
        //ID为API编号
        routeDefinition.setId(request.getApiCode());
        routeDefinition.setUri(new URI(request.getFullPath()));
        //元数据存储API所属部门，用于监控数据统计
        Map<String,Object> meta = new HashMap<>(1);
        meta.put(RouteInfoConstant.ORG_CODE, request.getMainOrgCode());
        routeDefinition.setMetadata(meta);
        //断言工厂
        routeDefinition.setPredicates(getPredicateList(request.getApiCode()));
        //过滤器链
        routeDefinition.setFilters(getFilterDefinition(request));
        return  routeDefinition;
    }

    @SneakyThrows
    public static RouteDefinition getHtmlRouteDefinition(ReleaseRequest request){
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(request.getHtmlInstanceCode());
        routeDefinition.setUri(new URI(request.getFullPath()));
        //元数据存储API所属部门，用于监控数据统计
        Map<String,Object> meta = new HashMap<>(1);
        meta.put(RouteInfoConstant.ORG_CODE, request.getMainOrgCode());
        routeDefinition.setMetadata(meta);
        routeDefinition.setPredicates(getPredicateList(request.getHtmlInstanceCode()));
        routeDefinition.setFilters(getFilterDefinition(request));
        return routeDefinition;
    }

    @SneakyThrows
    public static RouteDefinition getJsRouteDefinition(ReleaseRequest request){
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(request.getHtmlPredicatePath());
        routeDefinition.setUri(new URI(request.getFullPath()));
        routeDefinition.setPredicates(getPredicateList(request.getPredicatePath()));
        List<FilterDefinition> filterDefinitions = new ArrayList<>(1);
        filterDefinitions.add(getStripPrefixFilterDefinition());
        routeDefinition.setFilters(filterDefinitions);
        return routeDefinition;
    }


    public static List<PredicateDefinition> getPredicateList(String predicatePath){

        List<PredicateDefinition> predicateDefinitions = new ArrayList<>();
        PredicateDefinition path = new PredicateDefinition();
        path.setName(RouteInfoConstant.PATH_PREDICATE_FACTORY);
        if (predicatePath.startsWith(String.valueOf(CharPool.SLASH))){
            path.addArg("patterns", predicatePath + CharPool.SLASH + "**");
        }else {
            path.addArg("patterns", CharPool.SLASH + predicatePath + CharPool.SLASH + "**");
        }

        predicateDefinitions.add(path);
        return predicateDefinitions;
    }


    public static List<FilterDefinition> getFilterDefinition(ReleaseRequest request){
        List<FilterDefinition> filterDefinitionList = new ArrayList<>();
        //去除url中的参数过滤器，即系统中自定义的ApiCode
        filterDefinitionList.add(getStripPrefixFilterDefinition());
        FilterDefinition monitor = new FilterDefinition();
        monitor.setName(RouteInfoConstant.MONITOR_GATEWAY_FILTER);
        filterDefinitionList.add(monitor);
        if (!request.getAuthType().equals(ApiAuthType.PUBLIC)){
            //消费者过滤器
            FilterDefinition consumer = new FilterDefinition();
            consumer.setName(RouteInfoConstant.CONSUMER_PREDICATE_FACTORY);
            consumer.addArg("sources", getConsumer(request.getAppCodes()).toString());
            filterDefinitionList.add(consumer);
        }
        if (request.getAuthType().equals(ApiAuthType.AUTHOR)){
            //白名单过滤器
            FilterDefinition whiteIp = new FilterDefinition();
            whiteIp.setName(RouteInfoConstant.WHITE_IP_PREDICATE_FACTORY);
            filterDefinitionList.add(whiteIp);
        }
        //如果有配置鉴权模板，那么在转发下游之前增加请求头参数
        if (CollUtil.isNotEmpty(request.getAuthConfig())){
            Set<String> keys = request.getAuthConfig().keySet();
            for (String  key : keys){
                FilterDefinition addRequestHeader = new FilterDefinition();
                addRequestHeader.setName(RouteInfoConstant.ADD_REQUEST_HEADER_GATEWAY_FILTER);
                addRequestHeader.setArgs(request.getAuthConfig());
            }
        }
        return filterDefinitionList;
    }

    /**
     * 组装获取消费者断言对象
     */
    public static StringBuffer getConsumer(Set<String> consumerList){
        StringBuffer buffer = new StringBuffer();
        if (CollUtil.isNotEmpty(consumerList)){
            consumerList.stream().forEach(e->buffer.append(e + CharPool.COMMA));
            buffer.deleteCharAt(buffer.length()-1);
        }
        log.info(buffer.toString());
        return buffer;
    }

    /**
     * 单独获取去前缀的过滤器定义
     */
    public static FilterDefinition getStripPrefixFilterDefinition(){
        FilterDefinition stripPrefix = new FilterDefinition();
        stripPrefix.setName(RouteInfoConstant.STRIP_PREFIX_GATEWAY_FILTER);
        stripPrefix.addArg("parts","1");
        return stripPrefix;
    }
}
