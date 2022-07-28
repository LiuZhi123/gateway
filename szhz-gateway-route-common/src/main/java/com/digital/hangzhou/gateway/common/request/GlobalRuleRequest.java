package com.digital.hangzhou.gateway.common.request;

import com.digital.hangzhou.gateway.common.enums.ApiAuthType;
import lombok.Data;

@Data
public class GlobalRuleRequest {
    /**
     * 统一限流状态
     */
    private Boolean limitStatus;
    /**
     * 统一限流数值
     */
    private String limitRate;
    /**
     * 统一鉴权状态
     */
    private Boolean authStatus;
    /**
     * 统一鉴权类型
     */
    private ApiAuthType apiAuthType;

    /**
     * 应用中心配置开关状态
     */

    private Boolean applicationStatus;
    /**
     * 租户编号
     */
    private String tenantId;
}
