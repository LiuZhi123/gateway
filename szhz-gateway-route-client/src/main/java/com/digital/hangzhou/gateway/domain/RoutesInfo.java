package com.digital.hangzhou.gateway.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

/**
 * @author: xiaosl
 * @date: 2022-03-18-15:27
 * @description: APISIX 路由信息
 * 文档：https://apisix.apache.org/zh/docs/apisix/admin-api
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RoutesInfo {

	/**
	 * 路由主键
	 */
	private String id;


	/**
	 * 标识描述、使用场景等。	
	 */
	private String desc;

	/**
	 * 标识路由名称
	 */
	private String name;
	/**
	 * 匹配规则 	必选，不能与 uris 一起使用
	 * 除了如 /foo/bar、/foo/gloo 这种全量匹配外，使用不同 Router 还允许更高级匹配，更多见 Router。
	 *  示例："/hello/*"
	 */
	private String uri;

	/**
	 * 匹配规则  必选，不能与 uri 一起使用
	 * 非空数组形式，可以匹配多个 uri
	 *  示例：["/hello", "/world"]
	 */
	private List<String> uris;


	/**
	 * 匹配规则 可选	
	 * 可选，不能与 hosts 一起使用
	 * 当前请求域名，比如 foo.com；也支持泛域名，比如 *.foo.com。
	 * "foo.com"
	 */
	private String host;

	/**
	 * 匹配规则 可选	
	 * 可选，不能与 host 一起使用
	 * 非空列表形态的 host，表示允许有多个不同 host，匹配其中任意一个即可。
	 * ["foo.com", "*.bar.com"]
	 *
	 */
	private List<String> hosts;

	/**
	 * 匹配规则 可选	
	 * 可选，不能与 remote_addrs 一起使用
	 * 客户端请求 IP 地址: 192.168.1.101、192.168.1.102 以及 CIDR 格式的支持 192.168.1.0/24。特别的，APISIX 也完整支持 IPv6 地址匹配：::1，fe80::1, fe80::1/64 等。
	 * "192.168.1.0/24"
	 */
	private String remote_addr;

	/**
	 * 匹配规则 可选	
	 * 可选，不能与 remote_addr 一起使用
	 * 非空列表形态的 remote_addr，表示允许有多个不同 IP 地址，符合其中任意一个即可。
	 * ["127.0.0.1", "192.0.0.0/8", "::1"]
	 */
	private List<String> remote_addrs;

	/**
	 * 匹配规则 可选	
	 * 如果为空或没有该选项，代表没有任何 method 限制，也可以是一个或多个的组合：GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS，CONNECT，TRACE。
	 * ["GET", "POST"]
	 */
	private List<String> methods;

	/**
	 * 匹配规则 可选	 
	 * 如果不同路由包含相同 uri，根据属性 priority 确定哪个 route 被优先匹配，值越大优先级越高，默认值为 0。
	 * priority = 10
	 */
	private Integer priority;

	/**
	 * 匹配规则 可选
	 * 由一个或多个[var, operator, val]元素组成的列表，类似这样：[[var, operator, val], [var, operator, val], ...]]。例如：["arg_name", "==", "json"]，表示当前请求参数 name 是 json。这里的 var 与 Nginx 内部自身变量命名是保持一致，所以也可以使用 request_uri、host 等。更多细节请参考lua-resty-expr
	 * [["arg_name", "==", "json"], ["arg_age", ">", 18]]
	 */
	private String[] vars;

	/**
	 * 匹配规则 可选
	 * 用户自定义的过滤函数。可以使用它来实现特殊场景的匹配要求实现。该函数默认接受一个名为 vars 的输入参数，可以用它来获取 Nginx 变量。
	 * function(vars) return vars["arg_name"] == "json" end
	 */
	private String filter_func;

	/**
	 * 匹配规则 可选
	 * 标识附加属性的键值对
	 * {"version":"v2","build":"16","env":"production"}
	 */
	private Map<String, String> labels;

	/**
	 * 为 route 设置 upstream 的连接、发送消息、接收消息的超时时间。这个配置将会覆盖在 upstream 中 配置的 timeout 选项
	 * {"connect": 3, "send": 3, "read": 3}
	 */
	private Timeout timeout;

	/**
	 * 是否启用 websocket(boolean), 缺省 false.
	 */
	private Boolean enable_websocket;

	/**
	 *是否启用此路由, 缺省 1。
	 * 1 表示启用，0 表示禁用
	 */
	private Integer status;


	/**
	 * 启用的 Upstream 配置，详见 Upstream
	 */
	private UpstreamsInfo upstream;

	/**
	 * 	启用的 upstream id，详见 Upstream
	 */
	private String upstream_id;

	/**
	 * 绑定的 Service 配置，详见 Service
	 * Service 类型
	 */
	private String service_id;

	/**
	 * 可选 Plugin
	 * {
	 *     "proxy-rewrite": {
	 *       "regex_uri": [
	 *         "^/apisix/(.*)$",
	 *         "/$1"
	 *       ]
	 *     }
	 *   }
	 */
	private Map<String, Object> plugins;

	/**
	 * 可选，无法跟 script 一起配置
	 * 绑定的 Plugin config 配置，详见 Plugin config
	 */
	private String plugin_config_id;

	/**
	 * 可选 Script
	 */
	private String script;

	/**
	 * 创建时间
	 * 单位为秒的 epoch 时间戳，如果不指定则自动创建
	 */
	private long create_time = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));

	/**
	 * 更新时间
	 * 单位为秒的 epoch 时间戳，如果不指定则自动创建
	 */
	private long update_time = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));


}
