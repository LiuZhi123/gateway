package com.digital.hangzhou.gateway.common.request;

import com.digital.hangzhou.gateway.common.enums.ApiAuthType;
import lombok.Data;

import java.util.Set;

@Data
public class ReleaseAuthRequest {
    /**
     *api实例编号或者html实例编号
     */
    String apiInstanceCode;

    /**
     * 鉴权开启状态
     */
    ApiAuthType authType;

    /**
     * 应用编号集合
     */
    Set<String> appCodes;
}
