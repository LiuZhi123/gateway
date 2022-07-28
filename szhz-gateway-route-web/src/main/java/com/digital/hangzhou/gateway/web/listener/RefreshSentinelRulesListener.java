package com.digital.hangzhou.gateway.web.listener;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;


import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RefreshSentinelRulesListener implements MessageListener {

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("监听到同步sentinel规则事件");
        Map<String, GatewayFlowRule> cache = redisTemplate.opsForHash().entries(RedisConstant.SENTINEL_RULES);
        Set<GatewayFlowRule> rules = cache.values().stream().collect(Collectors.toSet());
        GatewayRuleManager.loadRules(rules);
        Object o = redisTemplate.opsForValue().get(RedisConstant.SYSTEM_RULES);
        List<SystemRule> systemRules = null == o ? new ArrayList<>(0) : (List<SystemRule>) o;
        SystemRuleManager.loadRules(systemRules);
    }
}
