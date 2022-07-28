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
     * 分组编号
     */
    String groupCode;

    /**
     * 界面实例编码
     */
    @Size(max = 32, message = "界面编码" + CommonConstant.ERROR_LENTH_MESSAGE + 32)
    private String htmlInstanceCode;

    /**
     * 分组下 需要禁止访问的API URL 列表
     */
    private List<String> filterList;

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
     * 分组/API描述
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
//	@NotNull(message = "传输协议不能为空")
    private ProtocolEnum serviceProtocol;

    /**
     * 上架分组/API服务地址
     */
//	@NotBlank(message = "上架分组/API服务地址不能为空")
    private String serviceAddress;

    /**
     *  上架分组/API服务端口
     */
//	@NotNull(message = "上架分组/API服务端口不能为空")
    private Integer servicePort;

    /**
     * 匹配地址 /api/*
     */
    private String predicatePath;

    /**
     * 单独上架API的时候  完整路径（不含ip、端口、域名）
     */
    private String fullPath;

    /**
     *界面请求断言
     */
    private String htmlPredicatePath;

    /**
     * Api配置
     */
    private String config;

    /**
     * 应用白名单
     */
    private Set<String> appCodes;

    /**
     *部门编号，用于基于部门维度统计数据
     */
    private String mainOrgCode;

    /**
     * 鉴权模板配置
     */
    private Map<String ,String> authConfig;

}