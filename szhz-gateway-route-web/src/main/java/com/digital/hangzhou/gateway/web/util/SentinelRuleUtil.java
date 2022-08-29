package com.digital.hangzhou.gateway.web.util;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.fastjson.JSONObject;
import com.digital.hangzhou.gateway.common.constant.ApiConstant;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import com.digital.hangzhou.gateway.common.enums.StatusEnum;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.digital.hangzhou.gateway.common.constant.ApiConstant.API_LIMIT_STATUS;

@Component
public class SentinelRuleUtil{
    @Resource
    private RedisTemplate redisTemplate;

    //根据配置生成限流规则加载到sentinel
    public void addGatewaySentinelRule(String id,String config){
        if (StrUtil.isBlank(config) || null == JSONObject.parseObject(config).get(ApiConstant.API_LIMIT_STATUS) || StatusEnum.DISABLE.name().equals(JSONObject.parseObject(config).get(ApiConstant.API_LIMIT_STATUS))){
            delGatewaySentinelRule(id);
            return;
        }
        //每次更新网关规则时需要是不同的Set对象，loadRules内部会判断
        GatewayFlowRule flowRule = new GatewayFlowRule();
        flowRule.setResource(id);
        flowRule.setIntervalSec(1);
        flowRule.setCount(JSONObject.parseObject(config).getIntValue(ApiConstant.API_LIMIT_RATE));
        redisTemplate.opsForHash().put(RedisConstant.SENTINEL_RULES, id ,flowRule);
        redisTemplate.convertAndSend(RedisConstant.REFRESH_SENTINEL_CHANNEL,"");
    }


    public void delGatewaySentinelRule(String id){
      redisTemplate.opsForHash().delete(RedisConstant.SENTINEL_RULES, id);
      redisTemplate.convertAndSend(RedisConstant.REFRESH_SENTINEL_CHANNEL,"");
    }


    public void systemRules(Boolean status,Double qps){
        redisTemplate.delete(RedisConstant.SYSTEM_RULES);
        if (status == false){
            redisTemplate.convertAndSend(RedisConstant.REFRESH_SENTINEL_CHANNEL, "");
            return;
        }
        SystemRule systemRule = new SystemRule();
        systemRule.setQps(qps);
        List<SystemRule> list = CollUtil.toList(systemRule);
        redisTemplate.opsForValue().set(RedisConstant.SYSTEM_RULES, list);
        redisTemplate.convertAndSend(RedisConstant.REFRESH_SENTINEL_CHANNEL,"");
    }
}
