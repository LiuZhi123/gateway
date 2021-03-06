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
}
