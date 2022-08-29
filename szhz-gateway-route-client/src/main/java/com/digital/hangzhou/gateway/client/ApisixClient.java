package com.digital.hangzhou.gateway.client;

import com.custom.starters.customwebspringbootstarters.core.result.R;
import com.digital.hangzhou.gateway.intercecptor.HeaderInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.Intercept;
import retrofit2.http.GET;
import retrofit2.http.Path;

@Intercept(handler = HeaderInterceptor.class)
@RetrofitClient(baseUrl = "${platform.apisix-url}")
public interface ApisixClient {
    /**
     * 根据上游Id获取upstreams信息
     */
    @GET("upstreams/{id}")
    public R<String> getUpstreamById(@Path("id") String id);

    /**
     * 获取所有的路由信息
     * @return
     */
    @GET("routes")
    public R<String> getRoutesInfo();
}
