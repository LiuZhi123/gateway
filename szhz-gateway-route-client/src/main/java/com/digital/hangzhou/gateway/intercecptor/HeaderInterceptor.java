package com.digital.hangzhou.gateway.intercecptor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.custom.starters.customwebspringbootstarters.core.result.R;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.BasePathMatchInterceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HeaderInterceptor extends BasePathMatchInterceptor {
    @Override
    protected Response doIntercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder()
                .addHeader("X-API-KEY", "edd1c9f034335f136f87ad84b625c8f1");
        Response response = chain.proceed(builder.build());
        if (null != response.body()) {
            byte[] respBytes = response.body().bytes();
            String responseBody = new String(respBytes);
            JSONObject object = JSON.parseObject(responseBody);
            if (!StrUtil.startWith(String.valueOf(response.code()), "2")) {
                String error_msg = object.getString("error_msg");
                if (StrUtil.isBlank(error_msg)) {
                    error_msg = object.getString("message");
                }
                return response.newBuilder()
                        .code(HttpStatus.OK.value())
                        .body(ResponseBody.create(response.body().contentType(), JSON.toJSONString(R.fail(response.code(), error_msg))))
                        .build();
            }
            if (null != object) {
                JSONObject node = object.getJSONObject("node");
                if (null != node) {
                    JSONArray nodes = node.getJSONArray("nodes");
                    if (null != nodes){
                        return response.newBuilder()
                                .code(HttpStatus.OK.value())
                                .body(ResponseBody.create(response.body().contentType(),JSON.toJSONString(R.ok(nodes.toJSONString()))))
                                .build();
                    }
                    else {
                        return response.newBuilder()
                                .code(HttpStatus.OK.value())
                                .body(ResponseBody.create(response.body().contentType(), JSON.toJSONString(R.ok(node.getString("value")))))
                                .build();
                    }

                }
            }
        }
        return response.newBuilder()
                .code(HttpStatus.OK.value())
                .body(ResponseBody.create(response.body().contentType(), JSON.toJSONString(R.ok())))
                .build();
    }
}
