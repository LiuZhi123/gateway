package com.digital.hangzhou.gateway.common.request;

import com.digital.hangzhou.gateway.common.constant.CommonConstant;
import com.digital.hangzhou.gateway.common.enums.ApiAuthType;
import com.digital.hangzhou.gateway.common.enums.ProtocolEnum;
import com.digital.hangzhou.gateway.common.enums.ReleaseStatusEnum;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: liuzhi
 * @date: 2022-07-11
 * @description: 上下架请求类
 */
@Accessors(chain = true)
@ApiModel(value = "ReleaseRequest", description = "生成路由请求类")
@Data
public class ReleaseRequest {

    /**
     * API 编号
     */
    @Size(max = 32, message = "接口编码" + CommonConstant.ERROR_LENTH_MESSAGE + 32)
    String apiCode;


    /**
     * 界面实例编码
     */
    @Size(max = 32, message = "界面编码" + CommonConstant.ERROR_LENTH_MESSAGE + 32)
    private String htmlInstanceCode;

    /**
     * 界面接入形式,标准-STANDARD，重定向-REDIRECT
     */
    private String htmlAccessType;


    /**
     * 安全控制：PUBLIC--公开；DISABLE--需授权
     */
    @NotNull(message = "安全控制类型" + CommonConstant.ERROR_BLANK_MESSAGE)
    private ApiAuthType authType;

    /**
     * 分组/API名称
     */
    @NotBlank(message = "API名称" + CommonConstant.ERROR_BLANK_MESSAGE)
    private String name;

    /**
     * API描述
     */
    private String description;

    /**
     * 上架状态：ONLINE-上架；OFFLINE-下架
     */
    @NotNull(message = "上架状态" + CommonConstant.ERROR_BLANK_MESSAGE)
    private ReleaseStatusEnum releaseStatus;

    /**
     * 传输协议
     */
    private ProtocolEnum serviceProtocol;

    /**
     * 上架分组/API服务地址
     */
    private String serviceAddress;

    /**
     *  上架分组/API服务端口
     */
    private Integer servicePort;

    /**
     * 转发下游的服务地址，用于RewritePath过滤器改写真实的请求地址
     */
    private String predicatePath;

    /**
     * 转发下游的主机地址，用于配置RouteDefinition的Uri属性
     */
    private String fullPath;

    /**
     * API的请求路径,用于配置请求路径的断言工厂及路由转发路径改写的过滤器
     */
    private String requestPath;

    /**
     *界面js路由路径
     */
    private String htmlPredicatePath;

    /**
     * api配置，目前用于解析限流信息, 配置路由侧的sentinel限流规则
     */
    private String config;

    /**
     * 基于应用信息鉴权时的应用白名单,请求时使用的apiKey须在此集合中时才放行，
     * 目前使用的DISABLE和AUTHOR的
     * 安全控制级别的白名单会在此集合中，基于签名的白名单在缓存中存储
     */
    private Set<String> appCodes;

    /**
     * 部门编号，用于基于部门维度统计数据
     */
    private String mainOrgCode;

    /**
     * 鉴权模板配置，在请求头中添加指定的kv，用于配置路由请求头过滤器
     */
    private Map<String ,String> authConfig;

}