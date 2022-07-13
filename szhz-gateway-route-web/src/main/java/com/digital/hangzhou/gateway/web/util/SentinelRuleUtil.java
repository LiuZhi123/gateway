package com.digital.hangzhou.gateway.web.util;


import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.fastjson.JSONObject;
import com.digital.hangzhou.gateway.common.constant.ApiConstant;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.common.enums.StatusEnum;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class SentinelRuleUtil{
    @Resource
    private RedisTemplate redisTemplate;

    //根据配置生成限流规则加载到sentinel,
    public void addGatewaySentinelRule(String apiCode,String config){
        //每次更新网关规则时需要是不同的Set对象，loadRules内部会判断
        Set<GatewayFlowRule> rule = GatewayRuleManager.getRules();
        String limitStatus = JSONObject.parseObject(config).getString(ApiConstant.API_LIMIT_STATUS);
        GatewayFlowRule flowRule = new GatewayFlowRule();
        flowRule.setResource(apiCode);
        flowRule.setCount(JSONObject.parseObject(config).getIntValue(ApiConstant.API_LIMIT_RATE));
        if (limitStatus.equals(StatusEnum.ENABLE.name())){
            rule.add(flowRule);
            redisTemplate.opsForSet().add(RedisConstant.SENTINEL_RULES, flowRule);
        }
        else{
            rule.remove(flowRule);
            redisTemplate.opsForSet().remove(RedisConstant.SENTINEL_RULES, flowRule);
        }

        GatewayRuleManager.loadRules(rule);
        //todo 发送redis键通知事件
    }

}
