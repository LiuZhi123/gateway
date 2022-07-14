package com.digital.hangzhou.gateway.web.util;


import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.fastjson.JSONObject;
import com.digital.hangzhou.gateway.common.constant.ApiConstant;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SentinelRuleUtil{
    @Resource
    private RedisTemplate redisTemplate;

    //根据配置生成限流规则加载到sentinel
    public void addGatewaySentinelRule(String id,String config){
        //每次更新网关规则时需要是不同的Set对象，loadRules内部会判断
        GatewayFlowRule flowRule = new GatewayFlowRule();
        flowRule.setResource(id);
        flowRule.setCount(JSONObject.parseObject(config).getIntValue(ApiConstant.API_LIMIT_RATE));
        redisTemplate.opsForHash().put(RedisConstant.SENTINEL_RULES, id ,flowRule);
        redisTemplate.convertAndSend(RedisConstant.REFRESH_SENTINEL_CHANNEL,"");
    }


    public void delGatewaySentinelRule(String id){
      redisTemplate.opsForHash().delete(RedisConstant.SENTINEL_RULES, id);
      redisTemplate.convertAndSend(RedisConstant.REFRESH_SENTINEL_CHANNEL,"");
    }
}
