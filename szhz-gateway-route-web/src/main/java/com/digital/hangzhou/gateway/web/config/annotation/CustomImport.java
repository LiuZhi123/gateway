package com.digital.hangzhou.gateway.web.config.annotation;

import com.digital.hangzhou.gateway.web.config.CustomBeanScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author lz
 * @date 2023/3/2 14:31
 */
@Import(CustomBeanScannerRegistrar.class)
@Target({ElementType.TYPE})
//Retention注解
//注解存在的生命周期，默认为CLASS级别，运行时需要动态获取注解信息时 需要设置为RUNTIME级别
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomImport {
    String basePackage();
}
