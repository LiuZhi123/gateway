package com.digital.hangzhou.gateway.web.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import com.digital.hangzhou.gateway.common.enums.ApiAuthType;
import com.digital.hangzhou.gateway.common.request.ReleaseRequest;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
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
        Map<String,Object> meta = new HashMap<>(2);
        meta.put(RouteInfoConstant.ORG_CODE, request.getMainOrgCode());
        meta.put(RouteInfoConstant.ROUTE_NAME, request.getName());
        routeDefinition.setMetadata(meta);
        //断言工厂
        List<PredicateDefinition> predicateDefinitions = new ArrayList<>(1);
        predicateDefinitions.add(getPredicateList(request.getRequestPath(),true));
        routeDefinition.setPredicates(predicateDefinitions);
        //过滤器链
        List<FilterDefinition> filterDefinitions =  getCommonFilterList(request);
        filterDefinitions.add(getApiRewritePathFilter(request.getRequestPath(), request.getPredicatePath()));
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
        Map<String,Object> meta = new HashMap<>(2);
        meta.put(RouteInfoConstant.ORG_CODE, request.getMainOrgCode());
        meta.put(RouteInfoConstant.ROUTE_NAME, request.getName());
        routeDefinition.setMetadata(meta);
        List<PredicateDefinition> predicateDefinitions = new ArrayList<>();
        predicateDefinitions.add(getPredicateList(request.getPredicatePath()));
        if (!request.getAuthType().equals(ApiAuthType.PUBLIC)){
            predicateDefinitions.add(getQueryPredicate());
        }
        routeDefinition.setPredicates(predicateDefinitions);
        List<FilterDefinition> filterDefinitions =  getCommonFilterList(request);
        filterDefinitions.add(getHtmlStripPrefixFilterDefinition());
        routeDefinition.setFilters(filterDefinitions);
        return routeDefinition;
    }

    @SneakyThrows
    public static RouteDefinition getHtmlAuthRouteDefinition(ReleaseRequest request){
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(request.getHtmlInstanceCode() + "_Auth");
        URI uri = new URI(request.getFullPath());
        String routeUri = new StringBuilder().append(uri.getScheme()).append(CharPool.COLON).append(CharPool.SLASH)
                .append(CharPool.SLASH).append(uri.getHost()).append(CharPool.COLON).append(uri.getPort()).toString();
        routeDefinition.setUri(new URI(routeUri));
        List<PredicateDefinition> predicateDefinitions = new ArrayList<>();
        predicateDefinitions.add(getPredicateList(request.getPredicatePath()));
        routeDefinition.setPredicates(predicateDefinitions);
        List<FilterDefinition> filterDefinitions =  new ArrayList<>();
        filterDefinitions.add(getHtmlStripPrefixFilterDefinition());
        routeDefinition.setFilters(filterDefinitions);
        routeDefinition.setOrder(1);
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
        List<PredicateDefinition> predicateDefinitions = new ArrayList<>(1);
        predicateDefinitions.add(getPredicateList(request.getHtmlPredicatePath()));
        routeDefinition.setPredicates(predicateDefinitions);
        return routeDefinition;
    }

    /**
     * A
     * @param predicatePath
     * @param isApi
     * @return
     */
    public static PredicateDefinition getPredicateList(String predicatePath , boolean isApi){
        PredicateDefinition path = new PredicateDefinition();
        path.setName(RouteInfoConstant.PATH_PREDICATE_FACTORY);
        path.addArg("patterns", predicatePath);
        return path;
    }

    public static PredicateDefinition getPredicateList(String predicatePath){
        PredicateDefinition path = new PredicateDefinition();
        path.setName(RouteInfoConstant.PATH_PREDICATE_FACTORY);
        if (predicatePath.startsWith(String.valueOf(CharPool.SLASH))){
            path.addArg("patterns", predicatePath + CharPool.SLASH + "**");
        }else {
            path.addArg("patterns", CharPool.SLASH + predicatePath + CharPool.SLASH + "**");
        }
        return path;
    }


    public static PredicateDefinition getQueryPredicate(){
        PredicateDefinition query = new PredicateDefinition();
        query.setName("Query");
        query.addArg("param","X-BG-HMAC-ACCESS-KEY");
        return query;
    }



    /**
     * 路径重写过滤器（API路由使用）
     * @param requestPath api请求路径
     * @param apiPath api实际访问路径
     * @return
     */
    public static FilterDefinition getApiRewritePathFilter(String requestPath, String apiPath){
        FilterDefinition rewrite = new FilterDefinition();
        rewrite.setName(RouteInfoConstant.REWRITE_PATH_GATEWAY_FILTER);
        String regexp = requestPath;
        String replacement = apiPath;
        if (!requestPath.startsWith("/")){
            regexp = "/" + regexp;
        }
        if (!apiPath.startsWith("/")){
            replacement = "/" + replacement;
        }
        rewrite.addArg("replacement", replacement);
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
                StringBuilder config = new StringBuilder(RouteInfoConstant.ADD_REQUEST_HEADER_GATEWAY_FILTER);
                config.append("=").append(key).append(CharPool.COMMA).append(request.getAuthConfig().get(key));
                FilterDefinition addRequestHeader = new FilterDefinition(config.toString());
                filterDefinitionList.add(addRequestHeader);
            }
        }
        return filterDefinitionList;
    }
}
