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
    HTTP_ERROR_404(404,"未找到路由"),

    /**
     * 下游通用异常
     */

    /**
     * 通用模块
     */
    HEADER_ERROR(100100401, "请求头信息缺失"),
    GATEWAY_CENTER_ERROR(100100402, "请求地址发生内部错误;请查看请求地址服务是否正常"),
    GATEWAY_CENTER_NOT_FOUND(100100403, "请求的地址资源不存在;请核对请求地址是否正确或请求地址资源服务是否正常"),
    METHOD_NOT_ALLOWED(100100404, "请求地址不支持当前Method模式"),
    BAD_REQUEST(100100405, "请求参数异常"),
    REQUEST_URL_ILLEGAL(100100406, "请求地址不合法，网关暂不支持访问非接口编号请求地址"),
    APP_CODE_ILLEGAL(100100407, "请求地址不合法，请求地址是非法的接口编号"),
    WHITE_LIST_NOT_EXIST(100100408, "接口访问白名单不存在"),
    APP_CODE_NOT_EXIST_PATH(100100409, "接口编号未匹配到对应的请求URI"),
    API_STATUS_NOT_ON(100100410, "接口信息不存在，上架API列表未搜索到此接口"),
    APP_INFO_FIND_NOT_URL(100100411, "匹配到的接口未获取到请求的URL"),
    GATEWAY_TIMEOUT_MESSAGE(100100412, "请求地址访问超时,请就核对请求地址是否正常"),

    /**
     * 业务报错
     */
    ROUTE_NOT_FOUNT(200100401, "根据路由编号查询路由信息失败");
    private Integer code;

    private String msg;
}
