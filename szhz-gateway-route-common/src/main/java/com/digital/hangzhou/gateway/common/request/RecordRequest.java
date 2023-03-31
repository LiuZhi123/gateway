package com.digital.hangzhou.gateway.common.request;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 同步落库的监控指标数据
 */
@Data
@AllArgsConstructor
public class RecordRequest {
    //应用编号
    private String appCode;

    //部门编号
    private String orgCode;

    //上游IP
    private String remoteIp;

    //上游Host
    private String remoteHost;

    //下游响应
    private String status;

    //命中路由
    private String routeId;

    //区域编码
    private String adCode;

    //请求参数
    private String requestParams;

    //响应时长秒
    private String responseTime;

    //响应长度 kb
    private String responseSize;

    //响应结果
    private String responseResult;

    //ak
    private String appKey;
}
