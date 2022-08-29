package com.digital.hangzhou.gateway.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author: xiaosl
 * @date: 2022-03-18-14:20
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Timeout {

	/**
	 * 发送消息的超时时间
	 */
	private int send = 15;

	/**
	 * 连接超时
	 */
	private int connect = 15;

	/**
	 * 接收消息的超时时间
	 */
	private int read = 15;
}
