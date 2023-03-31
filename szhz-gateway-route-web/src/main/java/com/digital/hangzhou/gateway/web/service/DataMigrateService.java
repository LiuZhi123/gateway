//package com.digital.hangzhou.gateway.web.service;
//
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.map.multi.CollectionValueMap;
//import cn.hutool.core.text.CharPool;
//import cn.hutool.core.util.StrUtil;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.digital.hangzhou.gateway.common.constant.RedisConstant;
//import com.digital.hangzhou.gateway.common.request.ReleaseRequest;
//import com.digital.hangzhou.gateway.domain.RoutesInfo;
//import com.digital.hangzhou.gateway.domain.UpstreamsInfo;
//import com.digital.hangzhou.gateway.service.ApisixService;
//import com.digital.hangzhou.gateway.web.util.RouteDefinitionUtil;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.gateway.filter.FilterDefinition;
//import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
//import org.springframework.cloud.gateway.route.RouteDefinition;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import java.io.*;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//@Slf4j
//@Service
//public class DataMigrateService {
//    @Resource
//    private RedisTemplate redisTemplate;
//    @Resource
//    private ApisixService apisixService;
//    @Value("${platform.route-file-path}")
//    private String path;
//
//    public int dataMigrate() {
//        List<RoutesInfo> routesInfoList = apisixService.getRoutesInfo();
//        //筛选状态为开启的路由
//        List<RoutesInfo> use = routesInfoList.stream().filter(r->r.getStatus().equals(1)).collect(Collectors.toList());
//        CollUtil.clear(routesInfoList);
//        Map<String, RouteDefinition> result = new HashMap<>();
//        for (RoutesInfo routesInfo : use){
//            RouteDefinition routeDefinition = new RouteDefinition();
//            //填充ID
//            String id = getId(routesInfo);
//            if (StrUtil.isBlank(id)){
//                log.info("路由" + routesInfo.getName() + "生成ID失败，请检查!");
//                continue;
//            }
//            routeDefinition.setId(getId(routesInfo));
//            if (result.containsKey(id)){
//                if (null == routesInfo.getPlugins() || null == routesInfo.getPlugins().get("prometheus")){
//                    continue;
//                }
//                else{
//                    JSONObject pro = (JSONObject) routesInfo.getPlugins().get("prometheus");
//                    if (true == pro.getBoolean("disable")){
//                        continue;
//                    }
//                 }
//            }
//            //填充元数据
//            Map<String,Object> meta = new HashMap<>(2);
//            if (CollUtil.isNotEmpty(routesInfo.getLabels())){
//                meta.put("orgCode",routesInfo.getLabels().get("mainOrgCode"));
//            }
//            meta.put("name", routesInfo.getName());
//            routeDefinition.setMetadata(meta);
//            //填充下游地址
//            UpstreamsInfo upstreamsInfo = routesInfo.getUpstream();
//            if (null == upstreamsInfo){
//                upstreamsInfo = apisixService.getUpstreamById(routesInfo.getUpstream_id());
//            }
//            if (null == upstreamsInfo){
//                log.info("路由"+ routesInfo.getName() +"获取上游信息失败，请检查！");
//                continue;
//            }
//            Map<String,Object>  nodes = upstreamsInfo.getNodes();
//            log.info("路由:" + routeDefinition.getId() + "的下游信息为: " + nodes.toString());
//            String path = null;
//            if (null != nodes.get("host")){
//                String host = nodes.get("host").toString();
//                String port = nodes.get("port").toString();
//                path = host + CharPool.COLON + port;
//            }
//            else {
//                path = nodes.keySet().stream().collect(Collectors.toList()).get(0);
//            }
//            String scheme = upstreamsInfo.getScheme();
//            StringBuilder url = new StringBuilder(scheme).append(CharPool.COLON).append(CharPool.SLASH)
//                    .append(CharPool.SLASH).append(path);
//            try {
//                routeDefinition.setUri(new URI(url.toString()));
//            } catch (URISyntaxException e) {
//                log.info("路由" + routesInfo.getName() + "的uri解析异常：" + url);
//                continue;
//            }
//            //填充鉴权过滤器，监控过滤器，界面需要填充stripPrefix顾虑器，接口需要填充路径重写过滤器，js路由需要特殊配置
//            //判断是否是js请求使用的路由，js路由不需要配置
//            if (CollUtil.isNotEmpty(routesInfo.getPlugins()) && null != routesInfo.getPlugins().get("prometheus")){
//                JSONObject prometheus = (JSONObject) routesInfo.getPlugins().get("prometheus");
//                if (false == prometheus.getBoolean("disable")){
//                    routeDefinition.setFilters(getFilters(routesInfo));
//                }
//            }
//            else {
////                log.info("判断为js请求的路由id：" + routesInfo.getId() + ",名称：" + routesInfo.getName());
//            }
//            //填充Path断言
//            routeDefinition.setPredicates(getPredicates(routesInfo));
//            if (StrUtil.startWith(id,"HTML")){
//                if (null != routesInfo.getVars() && routesInfo.getVars().length != 0){
//                    if ( null != routesInfo.getPlugins().get("consumer-restriction")){
//                        JSONObject pluginMeta = (JSONObject) routesInfo.getPlugins().get("consumer-restriction");
//                        List<String> consumer =  pluginMeta.getObject("whitelist",List.class);
//                        if (CollUtil.isNotEmpty(consumer) && !consumer.get(0).equals("NullConsumerList")){
//                            ReleaseRequest request = new ReleaseRequest();
//                            request.setHtmlInstanceCode(id);
//                            request.setFullPath(routeDefinition.getUri().toString());
//                            request.setPredicatePath(id);
//                            RouteDefinition auth = RouteDefinitionUtil.getHtmlAuthRouteDefinition(request);
//                            result.put(auth.getId(), auth);
//                        }
//                    }
//                }
//            }
//            result.put(routeDefinition.getId(), routeDefinition);
//        }
//        redisTemplate.opsForHash().putAll("gatewayRoutes", result);
//        return result.size();
//    }
//
//    /**
//     * 获取过滤器链
//     * @return
//     */
//    public List<FilterDefinition> getFilters(RoutesInfo routesInfo){
//        List<FilterDefinition> filterDefinitionList = new ArrayList<>();
//        //监控过滤器
//        FilterDefinition monitor = new FilterDefinition();
//        monitor.setName("Monitor");
//        filterDefinitionList.add(monitor);
//
//        //鉴权过滤器,生产暂无使用IP白名单插件，暂不考虑
//        if (null != routesInfo.getPlugins().get("consumer-restriction")){
//            FilterDefinition consumer = new FilterDefinition();
//            consumer.setName("Consumer");
//            JSONObject pluginMeta = (JSONObject) routesInfo.getPlugins().get("consumer-restriction");
//            List<String> consumerList =  pluginMeta.getObject("whitelist", List.class);
//            if (CollUtil.isNotEmpty(consumerList) && !consumerList.get(0).equals("NullConsumerList")){
//                StringBuilder sources = new StringBuilder();
//                for (String name:consumerList){
//                    sources.append("APP").append(name.split("APP")[1]).append(CharPool.COMMA);
//                }
//                sources.deleteCharAt(sources.length()-1);
//                consumer.addArg("sources",sources.toString());
//            }
//            else {
//                consumer.addArg("sources","");
//            }
//            filterDefinitionList.add(consumer);
//        }
//
//        //判断是否是API路由
//        String url = routesInfo.getUri();
//        if (StrUtil.isBlank(url)){
//            url = routesInfo.getUris().get(0);
//        }
//        if (url.startsWith("/HTML_")){
//            FilterDefinition stripPrefix = new FilterDefinition();
//            stripPrefix.setName("StripPrefix");
//            stripPrefix.addArg("parts","1");
//            filterDefinitionList.add(stripPrefix);
//        }
//        else {
//            if (null != routesInfo.getPlugins().get("proxy-rewrite")){
//                JSONObject proxyWrite = (JSONObject) routesInfo.getPlugins().get("proxy-rewrite");
////                log.info( routesInfo.getId() + "此路由的请求重写的插件：" + JSON.toJSONString(proxyWrite));
//                FilterDefinition rewritePath = new FilterDefinition();
//                rewritePath.setName("RewritePath");
//                rewritePath.addArg("regexp", url);
//                rewritePath.addArg("replacement", proxyWrite.get("uri").toString());
//                filterDefinitionList.add(rewritePath);
//            }
//
//        }
//        return filterDefinitionList;
//    }
//
//    /**
//     * 获取拦截器链
//     */
//    public List<PredicateDefinition> getPredicates(RoutesInfo routesInfo){
//        List<PredicateDefinition> predicateDefinitionList = new ArrayList<>();
//        PredicateDefinition predicateDefinition = new PredicateDefinition();
//        predicateDefinition.setName("Path");
//        String url = null;
//        if (StrUtil.isNotBlank(routesInfo.getUri())){
//            url = routesInfo.getUri();
//
//        }
//        else if (CollUtil.isNotEmpty(routesInfo.getUris())){
//            url = routesInfo.getUris().get(0);
//        }
//        if (url.endsWith("/*")){
//            url = url + "*";
//        }
//        predicateDefinition.addArg("patterns", url);
//        if ( null != routesInfo.getVars() && routesInfo.getVars().length != 0){
//            predicateDefinitionList.add(RouteDefinitionUtil.getQueryPredicate());
//        }
//        predicateDefinitionList.add(predicateDefinition);
//        return predicateDefinitionList;
//    }
//
//    /**
//     * 获取ID，统一化生成规则，对于以API_开头或HTML_开头的路由，截取编号作为ID，对非编号开头的路由，则取url作为ID
//     */
//    public String getId(RoutesInfo routesInfo){
//        String url = routesInfo.getUri();
//        if (StrUtil.isBlank(url)){
//            url = routesInfo.getUris().get(0);
//        }
//        if (StrUtil.isBlank(url)){
//            return null;
//        }
//        String id;
//        if (url.startsWith("/API_") || url.startsWith("/HTML_")){
//            id = url.split("/")[1];
//        }
//        else {
//            id = url;
//        }
//        return id;
//    }
//
//    /**
//     * 手动导入文件，刷新缓存
//     */
//    @SneakyThrows
//    public String importData(){
//        FileInputStream reader = new FileInputStream(path);
//        int size = reader.available();
//        byte[] in = new byte[size];
//        reader.read(in);
//        reader.close();
//        JSONObject object = JSONObject.parseObject(new String(in));
//        List<RouteDefinition> routes = object.getObject(RedisConstant.ROUTE_KEY, List.class);
//        Map<String,RouteDefinition> result  = new HashMap<>(routes.size());
//        routes.stream().map(r->result.put(r.getId(),r));
//        if (routes.size() == result.size()){
//            redisTemplate.delete(RedisConstant.ROUTE_KEY);
//            redisTemplate.opsForHash().putAll(RedisConstant.ROUTE_KEY, result);
//            return "还原备份文件成功，请重启服务刷新路由";
//        }
//        else {
//            return "读取的路由文件数据与备份文件中不一致，刷新失败";
//        }
//
//    }
//}
