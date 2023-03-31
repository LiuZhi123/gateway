package com.digital.hangzhou.gateway.web.exception;

import com.custom.starters.customwebspringbootstarters.core.exceptions.IErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessage implements IErrorCode {

    /**
     * HTTP 异常状态码
     * （抛出该错误，响应的状态码将被设置成对应的HTTP code，而非200）
     */
    HTTP_ERROR_401(401, "访问该路由需提供ak信息"),
    HTTP_ERROR_402(402,"路由ak校验失败，请检查ak是否正确"),
    HTTP_ERROR_403(403,"您的IP无访问此路由的权限"),

    /**
     * 下游通用异常
     */
    REFRESH_TOKEN_REQUEST_BAD(20001, "调用公共平台获取请求密钥失败"),

    /**
     * 通用模块
     */
    GATEWAY_CENTER_ERROR(500, "请求地址发生内部错误;请查看请求地址服务是否正常"),
    GATEWAY_CENTER_NOT_FOUND(404, "请求的地址资源不存在;请核对请求地址是否正确或请求地址资源服务是否正常"),
    METHOD_NOT_ALLOWED(405, "请求地址不支持当前Method模式"),
    BAD_REQUEST(400, "请求错误"),
    GATEWAY_TIMEOUT_MESSAGE(502, "请求地址访问超时,请就核对请求地址是否正常"),

    /**
     * 业务报错
     */
    HEADER_ERROR(10001, "请求头缺失appKey或sign或time信息"),
    SIGN_ERROR(10002, "签名信息验证失败"),
    TIME_ERROR(10003, "时间戳参数异常,请重新获取"),
    SIGN_EXPIRED(10004, "签名信息已过期,请重新获取"),
    APP_INFO_IS_NOT_AUTHORED_BY_API(10005, "该应用没有权限访问此路由"),

    ;
    private Integer code;

    private String msg;
}
