package com.digital.hangzhou.gateway.web.cache;

import org.springframework.cloud.gateway.route.RouteDefinition;



import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalCacheRepository {

    /**
     * RouteDefinition缓存
     */
    public static final Map<String,RouteDefinition> ROUTE_DEFINITION_CACHE = new ConcurrentHashMap<>();

}
