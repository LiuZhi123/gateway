package com.digital.hangzhou.gateway.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: liuzhi
 * @date: 2022-07-11
 * @description: 安全控制
 */
@Getter
@AllArgsConstructor
public enum ApiAuthType {
    PUBLIC("公开"),
    DISABLE("需授权"),
    AUTHOR("需授权+IP限制");
    private String name;
}
