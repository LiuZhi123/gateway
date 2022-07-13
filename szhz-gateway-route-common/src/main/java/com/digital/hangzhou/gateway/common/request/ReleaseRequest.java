package com.digital.hangzhou.gateway.common.request;

import com.digital.hangzhou.gateway.common.enums.ApiAuthType;
import com.digital.hangzhou.gateway.common.enums.ProtocolEnum;
import com.digital.hangzhou.gateway.common.enums.ReleaseStatusEnum;

import lombok.Data;

import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: liuzhi
 * @date: 2022-07-11
 * @description: 上下架请求类
 */
@Accessors(chain = true)
@Data
public class ReleaseRequest {

    /**
     * API 编号
     */
    String apiCode;

    /**
     * 分组编号
     */
    String groupCode;

    /**
     * 界面实例编码
     */
    private String htmlInstanceCode;

    /**
     * 分组下 需要禁止访问的API URL 列表
     */
    private List<String> filterList;

    /**
     * 安全控制：PUBLIC--公开；DISABLE--需授权(ak校验)；AUTHOR--ak校验+IP白名单校验
     */
    private ApiAuthType authType;

    /**
     * 分组/API名称
     */
    private String name;

    /**
     * 分组/API描述
     */
    private String description;

    /**
     * 上架状态：ONLINE-上架；OFFLINE-下架
     */
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
     * Ip白名单列表
     */
    private HashMap<String, List<String>> ipList;

    /**
     * 消费者白名单
     */
    private List<String> consumerList;

    /**
     *部门编号，用于基于部门维度统计数据
     */
    private String mainOrgCode;

    /**
     *
     */
    private Map<String, String> appInfo;
}