package com.digital.hangzhou.gateway.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HtmlAccessType {
    /**
     * 标准
     */
    STANDARD,
    /**
     * 重定向
     */
    REDIRECT;
}
