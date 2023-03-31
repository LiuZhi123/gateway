//package com.digital.hangzhou.gateway.service;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.custom.starters.customwebspringbootstarters.core.exceptions.CommonException;
//import com.custom.starters.customwebspringbootstarters.core.result.R;
//import com.digital.hangzhou.gateway.client.ApisixClient;
//import com.digital.hangzhou.gateway.domain.RoutesInfo;
//import com.digital.hangzhou.gateway.domain.UpstreamsInfo;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class ApisixService {
//    @Resource
//    private ApisixClient apisixClient;
//
//    public UpstreamsInfo getUpstreamById(String id) {
//        return processUpstream(apisixClient.getUpstreamById(id));
//    }
//
//    private UpstreamsInfo processUpstream(R<String> result) {
//        if (!result.isSuccess()) {
//            throw new CommonException(result.getCode(), result.getMsg());
//        }
//        return JSON.parseObject(result.getData(), UpstreamsInfo.class);
//    }
//
//    /**
//     * 获取所有路由信息
//     * @return
//     */
//    public List<RoutesInfo> getRoutesInfo(){
//        R<String> result = apisixClient.getRoutesInfo();
//        List<JSONObject> resultList = (List<JSONObject>) JSONArray.parse(result.getData());
//        List<RoutesInfo> routesInfoList = new ArrayList<>();
//        for (JSONObject jsonObject:resultList){
//            RoutesInfo routesInfo = JSON.parseObject(jsonObject.getString("value"), RoutesInfo.class);
//            routesInfoList.add(routesInfo);
//        }
//        return  routesInfoList;
//    }
//}
