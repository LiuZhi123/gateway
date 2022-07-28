package com.digital.hangzhou.gateway.common.request;

import com.digital.hangzhou.gateway.common.constant.CommonConstant;
import com.digital.hangzhou.gateway.common.enums.ApiAuthType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class ReleaseAuthRequest {
    /**
     *api实例编号或者html实例编号
     */
    @NotNull(message = "路由编号" + CommonConstant.ERROR_BLANK_MESSAGE)
    String apiInstanceCode;

    /**
     * 鉴权开启状态
     */
    @NotNull(message = "授权状态" + CommonConstant.ERROR_BLANK_MESSAGE)
    ApiAuthType authType;

    /**
     * 应用编号集合
     */
    @NotNull(message = "添加使用范围时应用编号" + CommonConstant.ERROR_BLANK_MESSAGE)
    Set<String> appCodes;
}
