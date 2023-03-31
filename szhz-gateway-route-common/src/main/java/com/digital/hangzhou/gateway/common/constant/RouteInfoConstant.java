package com.digital.hangzhou.gateway.common.constant;

public class RouteInfoConstant {



    public static final String API_KEY = "apiKey";

    public static final String ROUTE_NAME = "name";

    public static final String ROUTE_PATH = "path";

    /**
     * 过滤器/断言 工厂的名称
     */
    public static final String WHITE_IP_PREDICATE_FACTORY = "WhiteIp";

    public static final String CONSUMER_PREDICATE_FACTORY = "Consumer";

    public static final String STRIP_PREFIX_GATEWAY_FILTER = "StripPrefix";

    public static final String PATH_PREDICATE_FACTORY = "Path";

    public static final String ADD_REQUEST_HEADER_GATEWAY_FILTER = "AddRequestHeader";

    public static final String MONITOR_GATEWAY_FILTER = "Monitor";

    public static final String REWRITE_PATH_GATEWAY_FILTER = "RewritePath";

    public static final String REDIRECT_GATEWAY_FILTER = "Redirect";

    public static final String SIGN_FACTORY = "Sign";

    public static final String REFRESH_TOKEN_FACTORY = "RefreshToken";

    /**
     * 路由监控指标
     */
    public static final String ORG_CODE = "orgCode";

    public static final String APP_CODE = "appCode";

    public static final String REMOTE_IP = "remoteIp";

    public static final String REMOTE_HOST = "remoteHost";

    public static final String RESPONSE_STATUS = "status";

    public static final String ROUTE_ID  = "routeId";

    public static final String REQUEST_PARAMS = "requestParam";

    /**
     * 流量统计指标
     */
    public static final String BANDWIDTH_TYPE = "type";

    public static final String BANDWIDTH_PATH = "path";

}
