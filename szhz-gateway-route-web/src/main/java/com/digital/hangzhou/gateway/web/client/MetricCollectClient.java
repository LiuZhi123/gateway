package com.digital.hangzhou.gateway.web.client;

import com.digital.hangzhou.gateway.common.request.RecordRequest;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


@RetrofitClient(baseUrl = "${platform.gateway-base-url}")
public interface MetricCollectClient {

    @POST("record/save")
    Call<Void> record(@Body RecordRequest request);
}
