package com.digital.hangzhou.gateway.web.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * @author lz
 * @date 2022/12/3 12:02
 */
@Slf4j
public class IpUtil {

    private static String remoteIp = "RemoteIp";

    private static String remoteHost = "RemoteHost";

    private static String xForwardedFor = "X-Forwarded-For";

    private static String xRealIp = "X-Real-IP";

    public static String getRemoteIp(ServerHttpRequest request){
        String result =  request.getRemoteAddress().getAddress().getHostAddress();
        HttpHeaders headers = request.getHeaders();
        if (headers.containsKey(remoteIp)){
            result = headers.get(remoteIp).get(0);
        }
        if (headers.containsKey(xForwardedFor)){
            result = headers.get(xForwardedFor).get(0);
        }
        if (headers.containsKey(xRealIp)){
            result = headers.get(xRealIp).get(0);
        }
        if (headers.containsKey(remoteHost)){
            result = headers.get(remoteHost).get(0);
        }
        if (StrUtil.equals(result ,"0:0:0:0:0:0:0:1")){
            result = "127.0.0.1";
        }
        return result;
    }
}
