package com.digital.hangzhou.gateway.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: liuzhi
 * @date: 2022-07-11
 * @description: 上线状态
 */
@Getter
@AllArgsConstructor
public enum ReleaseStatusEnum {

    /**
     * 上架
     */
    ONLINE,

    /**
     * 下架
     */
    OFFLINE;
}