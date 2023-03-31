package com.digital.hangzhou.gateway.web.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.StrUtil;
import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import com.digital.hangzhou.gateway.common.enums.HtmlAccessType;
import com.digital.hangzhou.gateway.common.request.ReleaseRequest;
import com.digital.hangzhou.gateway.web.factory.AuthTypeAnnotationFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URI;
import java.util.*;

@Slf4j
@Component
public class RouteDefinitionUtil {

    @Resource
    private AuthTypeAnnotationFactory authTypeAnnotationFactory;
    /**
     * 根据request请求生成routeDefinition对象
     * @return`
     */
    @SneakyThrows
    public RouteDefinition getApiRouteDefinition(ReleaseRequest request){
        RouteDefinition routeDefinition = new RouteDefinition();
        //ID为API编号
        routeDefinition.setId(request.getApiCode());
        routeDefinition.setUri(new URI(request.getFullPath()));
        //元数据存储API所属部门，用于监控数据统计
        Map<String,Object> meta = new HashMap<>(2);
        meta.put(RouteInfoConstant.ORG_CODE, request.getMainOrgCode());
        meta.put(RouteInfoConstant.ROUTE_NAME, request.getName());
        routeDefinition.setMetadata(meta);
        //断言工厂,目前只支持以请求路径匹配,通用请求路径断言为  服务地址/api编号
        List<PredicateDefinition> predicateDefinitions = new ArrayList<>(1);
        predicateDefinitions.add(getPredicateList(request.getRequestPath(),true));
        routeDefinition.setPredicates(predicateDefinitions);
        //业务通用过滤器链,包含了监控过滤器,鉴权过滤器,下游请求头模板过滤器
        List<FilterDefinition> filterDefinitions =  getCommonFilterList(request);
        //路径改写过滤器,将请求路径重写为下游的真实路径
        filterDefinitions.add(getApiRewritePathFilter(request.getRequestPath(), request.getPredicatePath()));
        routeDefinition.setFilters(filterDefinitions);
        return routeDefinition;
    }

    @SneakyThrows
    public RouteDefinition getHtmlRouteDefinition(ReleaseRequest request){
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(request.getHtmlInstanceCode());
        URI uri = new URI(request.getFullPath());
        String routeUri = new StringBuilder().append(uri.getScheme()).append(CharPool.COLON).append(CharPool.SLASH)
                .append(CharPool.SLASH).append(uri.getHost()).append(CharPool.COLON).append(uri.getPort()).toString();
        routeDefinition.setUri(new URI(routeUri));
        //元数据存储API所属部门，用于监控数据统计
        Map<String,Object> meta = new HashMap<>(3);
        meta.put(RouteInfoConstant.ORG_CODE, request.getMainOrgCode());
        meta.put(RouteInfoConstant.ROUTE_NAME, request.getName());
        meta.put(RouteInfoConstant.ROUTE_PATH, request.getFullPath());
        routeDefinition.setMetadata(meta);
        List<PredicateDefinition> predicateDefinitions = new ArrayList<>();
        predicateDefinitions.add(getPredicateList(request.getPredicatePath()));
        //如果是非重定向的界面，则不需要增加查询参数过滤
        if (request.getHtmlAccessType() == HtmlAccessType.STANDARD.name())
            predicateDefinitions.add(getQueryPredicate());
        routeDefinition.setPredicates(predicateDefinitions);
        List<FilterDefinition> filterDefinitions =  getCommonFilterList(request);
        if (StrUtil.isNotBlank(request.getHtmlAccessType()) && StrUtil.equals(HtmlAccessType.REDIRECT.name(), request.getHtmlAccessType()))
            filterDefinitions.add(getRedirectFilterDefinition());
        else
            filterDefinitions.add(getHtmlStripPrefixFilterDefinition());
        routeDefinition.setFilters(filterDefinitions);
        return routeDefinition;
    }

    @SneakyThrows
    public RouteDefinition getHtmlAuthRouteDefinition(ReleaseRequest request){
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(request.getHtmlInstanceCode() + "_Auth");
        URI uri = new URI(request.getFullPath());
        String routeUri = new StringBuilder().append(uri.getScheme()).append(CharPool.COLON).append(CharPool.SLASH)
                .append(CharPool.SLASH).append(uri.getHost()).append(CharPool.COLON).append(uri.getPort()).toString();
        routeDefinition.setUri(new URI(routeUri));
        Map<String,Object> meta = new HashMap<>(1);
        meta.put(RouteInfoConstant.ROUTE_NAME, request.getName());
        routeDefinition.setMetadata(meta);
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
    public  RouteDefinition getJsRouteDefinition(ReleaseRequest request){
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(request.getHtmlPredicatePath());
        URI uri = new URI(request.getFullPath());
        String routeUri = new StringBuilder().append(uri.getScheme()).append(CharPool.COLON).append(CharPool.SLASH)
                .append(CharPool.SLASH).append(uri.getHost()).append(CharPool.COLON).append(uri.getPort()).toString();
        routeDefinition.setUri(new URI(routeUri));
        Map<String,Object> meta = new HashMap<>(1);
        meta.put(RouteInfoConstant.ROUTE_NAME, request.getName());
        routeDefinition.setMetadata(meta);
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
    private PredicateDefinition getPredicateList(String predicatePath , boolean isApi){
        PredicateDefinition path = new PredicateDefinition();
        path.setName(RouteInfoConstant.PATH_PREDICATE_FACTORY);
        path.addArg("patterns", predicatePath);
        return path;
    }

    private PredicateDefinition getPredicateList(String predicatePath){
        PredicateDefinition path = new PredicateDefinition();
        path.setName(RouteInfoConstant.PATH_PREDICATE_FACTORY);
        if (predicatePath.startsWith(String.valueOf(CharPool.SLASH))){
            path.addArg("patterns", predicatePath + CharPool.SLASH + "**");
        }else {
            path.addArg("patterns", CharPool.SLASH + predicatePath + CharPool.SLASH + "**");
        }
        return path;
    }

    private PredicateDefinition getQueryPredicate(){
        PredicateDefinition query = new PredicateDefinition();
        query.setName("Query");
        query.addArg("param","X-BG-HMAC-ACCESS-KEY");
        return query;
    }



    /**
     * 路径重写过滤器（API路由使用）
     * @param requestPath api请求路径
     * @param apiPath 下游接口地址
     * @return
     */
    private FilterDefinition getApiRewritePathFilter(String requestPath, String apiPath){
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
    private  FilterDefinition getHtmlStripPrefixFilterDefinition(){
        //消费者过滤器
        Map<String,String> param = new HashMap<String, String>(1){{
            put("parts", "1");
        }};
        return getAuthFilter(RouteInfoConstant.STRIP_PREFIX_GATEWAY_FILTER, param);
    }

    /**
     *路由通用过滤器链组装  包含监控插件，消费者白名单，鉴权模板插件
     */
    private List<FilterDefinition> getCommonFilterList(ReleaseRequest request){
        List<FilterDefinition> filterDefinitionList = new ArrayList<>();
        filterDefinitionList.add(getAuthFilter(RouteInfoConstant.MONITOR_GATEWAY_FILTER, null));
        List<FilterDefinition> authFilters =  authTypeAnnotationFactory.getFilterDefinitions(request);
        if (CollUtil.isNotEmpty(authFilters))
            filterDefinitionList.addAll(authFilters);
        //如果有配置鉴权模板，那么在转发下游之前增加请求头参数
        if (CollUtil.isNotEmpty(request.getAuthConfig())){
           FilterDefinition header = new FilterDefinition();
           header.setName(RouteInfoConstant.REFRESH_TOKEN_FACTORY);
           header.addArg("appKey", request.getAuthConfig().get("AK"));
           header.addArg("secretKey", request.getAuthConfig().get("SK"));
           filterDefinitionList.add(header);
        }
        return filterDefinitionList;
    }

    /**
     * 获取界重定向过滤器
     */
    private FilterDefinition  getRedirectFilterDefinition(){
        FilterDefinition redirect = new FilterDefinition();
        redirect.setName(RouteInfoConstant.REDIRECT_GATEWAY_FILTER);
        return redirect;
    }

    /**
     * 根据名称和参数获取过滤器定义实例
     * @param name
     * @param param
     * @return
     */
    public static FilterDefinition getAuthFilter(String name, Map<String, String> param) {
        FilterDefinition filterDefinition = new FilterDefinition();
        filterDefinition.setName(name);
        if (CollUtil.isNotEmpty(param)){
            param.keySet().stream().forEach(e->{
                filterDefinition.addArg(e, param.get(e));
            });
        };
        return filterDefinition;
    }
}
