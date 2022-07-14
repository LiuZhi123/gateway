package com.digital.hangzhou.gateway.web.listener;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;


import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class RefreshSentinelRulesListener implements MessageListener {

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("监听到同步sentinel规则事件");
        //如果有多个线程同时在修改规则，每次都从redis读取了最新的配置，最终结果都会与redis保持一致
        Map<String, GatewayFlowRule> cache = redisTemplate.opsForHash().entries(RedisConstant.SENTINEL_RULES);
        Set<GatewayFlowRule> rules = (Set<GatewayFlowRule>) cache.values();
        GatewayRuleManager.loadRules(rules);
    }
}
