package com.digital.hangzhou.gateway.web.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
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
        List<FilterDefinition> filterDefinitions =  getCommonFilterList(request);
        filterDefinitions.add(getApiRewritePathFilter(request.getApiCode(), request.getPredicatePath()));
        routeDefinition.setFilters(filterDefinitions);
        return  routeDefinition;
    }

    @SneakyThrows
    public static RouteDefinition getHtmlRouteDefinition(ReleaseRequest request){
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(request.getHtmlInstanceCode());
        URI uri = new URI(request.getFullPath());
        String routeUri = new StringBuilder().append(uri.getScheme()).append(CharPool.COLON).append(CharPool.SLASH)
                .append(CharPool.SLASH).append(uri.getHost()).append(CharPool.COLON).append(uri.getPort()).toString();
        routeDefinition.setUri(new URI(routeUri));
        //元数据存储API所属部门，用于监控数据统计
        Map<String,Object> meta = new HashMap<>(1);
        meta.put(RouteInfoConstant.ORG_CODE, request.getMainOrgCode());
        routeDefinition.setMetadata(meta);
        routeDefinition.setPredicates(getPredicateList(request.getHtmlInstanceCode()));
        List<FilterDefinition> filterDefinitions =  getCommonFilterList(request);
        filterDefinitions.add(getHtmlStripPrefixFilterDefinition());
        routeDefinition.setFilters(filterDefinitions);
        return routeDefinition;
    }

    @SneakyThrows
    public static RouteDefinition getJsRouteDefinition(ReleaseRequest request){
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(request.getHtmlPredicatePath());
        URI uri = new URI(request.getFullPath());
        String routeUri = new StringBuilder().append(uri.getScheme()).append(CharPool.COLON).append(CharPool.SLASH)
                .append(CharPool.SLASH).append(uri.getHost()).append(CharPool.COLON).append(uri.getPort()).toString();
        routeDefinition.setUri(new URI(routeUri));
        routeDefinition.setPredicates(getPredicateList(request.getHtmlPredicatePath()));
        List<FilterDefinition> filterDefinitions = new ArrayList<>(1);
        filterDefinitions.add(getHtmlStripPrefixFilterDefinition());
//        routeDefinition.setFilters(filterDefinitions);
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



    /**
     * 路径重写过滤器（API路由使用）
     * @param apiCode api编号
     * @param apiPath api请求路径
     * @return
     */
    public static FilterDefinition getApiRewritePathFilter(String apiCode, String apiPath){
        FilterDefinition rewrite = new FilterDefinition();
        rewrite.setName(RouteInfoConstant.REWRITE_PATH_GATEWAY_FILTER);
        String regexp = "/" + apiCode;
        if (apiPath.startsWith("/")){
            rewrite.addArg("replacement", apiPath);
        }
        else {
            rewrite.addArg("replacement", "/" + apiPath);
        }
        rewrite.addArg("regexp", regexp);
        return rewrite;
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
     * 获取去前缀的过滤器(界面路由及js路由使用)
     */
    public static FilterDefinition getHtmlStripPrefixFilterDefinition(){
        FilterDefinition stripPrefix = new FilterDefinition();
        stripPrefix.setName(RouteInfoConstant.STRIP_PREFIX_GATEWAY_FILTER);
        stripPrefix.addArg("parts","1");
        return stripPrefix;
    }

    /**
     *路由通用过滤器链组装  包含监控插件，消费者白名单，鉴权模板插件
     */
    public static List<FilterDefinition> getCommonFilterList(ReleaseRequest request){
        List<FilterDefinition> filterDefinitionList = new ArrayList<>();
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
}
