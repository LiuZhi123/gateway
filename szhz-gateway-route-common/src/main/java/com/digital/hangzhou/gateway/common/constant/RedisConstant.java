package com.digital.hangzhou.gateway.common.constant;

public class RedisConstant {

    /**
     * 网关路由存储
     */
    public static final String ROUTE_KEY = "gatewayRoutes";

    /**
     * sentinel路由限流规则的存储
     */
    public static final String SENTINEL_RULES = "sentinelRules";

    /**
     * redis增加路由的通知key
     */
    public static final String ADD_ROUTES_CHANNEL = "addRoutes";

    /**
     * redis删除路由的通知key
     */
    public static final String DELETE_ROUTES_CHANNEL = "delRoutes";

    /**
     * redis刷新限流规则的通知key
     */
    public static final String REFRESH_SENTINEL_CHANNEL = "refreshSentinel";

    /**
     * 存储系统限流规则
     */
    public static final String SYSTEM_RULES = "systemRules";


    /**
     * 统一网关配置
     */
    public static final String GATEWAY_GLOBAL_CONFIG = "gatewayGlobalConfig";

    /**
     * 应用前缀
     */
    public static final String SZHZ_Sign_PREFIX_KEYS = "szhz_SignPrefix:";
}
