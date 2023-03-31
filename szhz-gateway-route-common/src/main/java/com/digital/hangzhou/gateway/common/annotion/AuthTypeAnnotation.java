package com.digital.hangzhou.gateway.common.annotion;

import com.digital.hangzhou.gateway.common.enums.ApiAuthType;

import java.lang.annotation.*;

/**
 * @author lz
 * @date 2023/3/2 11:40
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AuthTypeAnnotation {

    ApiAuthType value();

}
