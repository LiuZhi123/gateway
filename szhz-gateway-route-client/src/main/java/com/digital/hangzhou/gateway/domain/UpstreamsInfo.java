package com.digital.hangzhou.gateway.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author: xiaosl
 * @date: 2022-03-18-14:08
 * @description: 上游信息对象
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UpstreamsInfo {


	/**
	 * 上游主键
	 */
	private String id;

	/**
	 * 	使用底层的 Nginx 重试机制将请求传递给下一个上游，默认启用重试且次数为后端可用的 node 数量。如果指定了具体重试次数，它将覆盖默认值。0 代表不启用重试机制。
	 * 	可选
	 */
	private Integer retries;

	/**
	 * 限制是否继续重试的时间，若之前的请求和重试请求花费太多时间就不再继续重试。0 代表不启用重试超时机制。
	 */
	private Integer retry_timeout;

	/**
	 * 标识上游服务名称、使用场景等。
	 */
	private String name;

	/**
	 * upstream 描述
	 */
	private String desc;
	/**
	 * 连接池
	 */
	private keepAlivePool keepalive_pool;


	/**
	 *请求发给上游时的 host 设置选型。 [pass，node，rewrite] 之一，默认是pass。pass: 将客户端的 host 透传给上游； node: 使用 upstream node 中配置的 host； rewrite: 使用配置项 upstream_host 的值。
	 */
	private String pass_host;

	/**
	 * 指定上游请求的 host，只在 pass_host 配置为 rewrite 时有效。
	 */
	private String upstream_host;

	/**
	 * 	跟上游通信时使用的 scheme。对于 7 层代理，需要是 ['http', 'https', 'grpc', 'grpcs'] 其中的一个。对于 4 层代理，需要是 ['tcp', 'udp', 'tls'] 其中的一个。默认是 'http'。
	 */
	private String scheme;


	/**
	 * 必需，不能和 service_name 一起用
	 * 哈希表或数组。当它是哈希表时，内部元素的 key 是上游机器地址列表，格式为地址 + （可选的）端口，其中地址部分可以是 IP 也可以是域名，
	 * 比如 192.168.1.100:80、foo.com:80等。value 则是节点的权重。当它是数组时，数组中每个元素都是一个哈希表，其中包含 host、weight 以及可选的 port、priority。
	 * nodes 可以为空，这通常用作占位符。客户端命中这样的上游会返回 502。
	 */
	private Map<String, Object> nodes;

	/**
	 * 必需，不能和 nodes 一起用
	 * 服务发现时使用的服务名，见集成服务发现注册中心
	 */
	private String service_name;

	/**
	 * 必需，如果设置了 service_name	; 服务发现类型，见集成服务发现注册中心
	 */
	private String discovery_type;

	/**
	 *   条件必需
	 *   该选项只有类型是 chash 才有效。根据 key 来查找对应的 node id，相同的 key 在同一个对象中，永远返回相同 id，
	 *   目前支持的 Nginx 内置变量有 uri, server_name, server_addr, request_uri, remote_port, remote_addr, query_string, host, hostname, arg_***，
	 *   其中 arg_*** 是来自 URL 的请求参数，Nginx 变量列表
	 */
	private String key;

	/**
	 * 配置健康检查的参数，详细可参考 https://apisix.apache.org/zh/docs/apisix/health-check
	 */
	private String checks;


	/**
	 * 负载均衡算法:
	 * roundrobin: 带权重的 roundrobin
	 * chash: 一致性哈希
	 * ewma: 选择延迟最小的节点，计算细节参考 https://en.wikipedia.org/wiki/EWMA_chart
	 * least_conn: 选择 (active_conn + 1) / weight 最小的节点。注意这里的 active connection 概念跟 Nginx 的相同：它是当前正在被请求使用的连接。
	 * 用户自定义的 balancer，需要可以通过 require("apisix.balancer.your_balancer") 来加载。
	 */
	private String type;

	/**
	 * 设置连接、发送消息、接收消息的超时时间
	 */
	private Timeout timeout;

	/**
	 * hash_on 支持的类型有 vars（Nginx 内置变量），header（自定义 header），cookie，consumer，默认值为 vars
	 */
	private String hash_on;

	/**
	 * 单位为秒的 epoch 时间戳，如果不指定则自动创建	
	 */
	private Long update_time;

	/**
	 * 单位为秒的 epoch 时间戳，如果不指定则自动创建
	 */
	private Long create_time;

	/**
	 * 1 表示启用，0 表示禁用
	 */
	private Integer status;

	/**
	 * 标识附加属性的键值对
	 */
	private String labels;


}
