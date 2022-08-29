package com.digital.hangzhou.gateway.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author: xiaosl
 * @date: 2022-03-18-14:04
 * @description: APISIX 连接池 配置
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class keepAlivePool {

	/**
	 * 容量
	 */
	private int size = 320;

	/**
	 * 空闲超时时间
	 */
	private int idle_timeout = 60;

	/**
	 * 请求数量
	 */
	private int requests = 1000;
}
