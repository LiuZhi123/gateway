//package com.digital.hangzhou.gateway.web.util;
//
//import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
//import io.prometheus.client.CollectorRegistry;
//import io.prometheus.client.Counter;
//import org.springframework.beans.BeansException;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.stereotype.Component;
//
//
///**
// * 初始化collectRegistry时添加自定义指标收集器
// */
//@Component
//public class CustomizeMetric implements ApplicationContextAware {
//
//    private static CustomizeMetric instance;
//    //路由访问数量的Count类型记录器
//    private Counter routeCounter;
//    //网关流量记录器
//    private Counter bandWidthCounter;
//
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        instance = this;
//        CollectorRegistry collectorRegistry = applicationContext.getBean(CollectorRegistry.class);
//        routeCounter = Counter.build().name("route_request_total").help("网关转发路由").labelNames(RouteInfoConstant.APP_CODE ,
//                        RouteInfoConstant.ORG_CODE, RouteInfoConstant.REMOTE_IP, RouteInfoConstant.REMOTE_HOST,
//                        RouteInfoConstant.ROUTE_ID, RouteInfoConstant.RESPONSE_STATUS,RouteInfoConstant.REQUEST_PARAMS)
//                .register(collectorRegistry);
//
//        bandWidthCounter = Counter.build().name("route_bandwidth").help("网关流量统计").labelNames(RouteInfoConstant.BANDWIDTH_TYPE,
//                        RouteInfoConstant.ROUTE_ID, RouteInfoConstant.APP_CODE, RouteInfoConstant.ORG_CODE, RouteInfoConstant.REMOTE_IP,
//                        RouteInfoConstant.REMOTE_HOST)
//                .register(collectorRegistry);
//
//    }
//
//    public Counter getRouteCounter(){
//        return routeCounter;
//    }
//
//    public Counter getBandWidthCounter(){
//        return bandWidthCounter;
//    }
//
//    public static CustomizeMetric getInstance(){
//        return instance;
//    }
//}
