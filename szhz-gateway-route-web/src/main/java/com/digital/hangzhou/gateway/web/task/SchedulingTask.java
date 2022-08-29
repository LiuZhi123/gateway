package com.digital.hangzhou.gateway.web.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.digital.hangzhou.gateway.common.constant.RedisConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class SchedulingTask {
    @Resource
    private RedisTemplate redisTemplate;
    //文件导出位置
    @Value("${platform.route-file-path}")
    String path;
    //定时任务的redis分布式锁
    private static final String SCHEDULING_TASK_LOCK = "task-lock";

    /**
     * 定时将缓存中的路由数据落入服务器磁盘文件中
     */
    @Async("taskExecutor")
    @Scheduled(cron = "0 0/30 * * * ?")
    public void saveRouteIntoFile(){
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(SCHEDULING_TASK_LOCK,1,3, TimeUnit.SECONDS);
        if (!flag){
            return;
        }
        try {
            File file = new File(path);
            if (file.exists()){
                file.delete();
            }
            file.createNewFile();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(path));
            //从缓存中加载路由信息
            List<RouteDefinition> routeDefinitionList = redisTemplate.opsForHash().values(RedisConstant.ROUTE_KEY);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(RedisConstant.ROUTE_KEY, routeDefinitionList);
            try {
                outputStreamWriter.write(jsonObject.toJSONString());
                outputStreamWriter.flush();
                outputStreamWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            redisTemplate.delete(SCHEDULING_TASK_LOCK);
        }
    }
}
