package com.digital.hangzhou.gateway.common.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Set;

/**
 * @author lz
 * @date 2022/12/1 10:20
 */
@Data
public class GlobalConfig {

    @ApiModelProperty("统一网关限流状态")
    private Boolean limitStatus;

    @ApiModelProperty("统一网关限流值")
    private String limitRate;

    @ApiModelProperty("租户配置开关")
    private Boolean applicationStatus;

    @ApiModelProperty("租户配置值")
    private String tenantId;

    @ApiModelProperty("监控参数开关")
    private Boolean monitorParam;

    @ApiModelProperty("请求入参配置")
    private Boolean paramDisplay;

    @ApiModelProperty("统一网关白名单状态")
    private Boolean whiteIpStatus = false;

    @ApiModelProperty("统一网关白名单列表")
    private Set<String> whiteIpSet = null;
}
