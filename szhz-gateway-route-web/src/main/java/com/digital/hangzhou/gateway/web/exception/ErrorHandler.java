package com.digital.hangzhou.gateway.web.exception;

import com.alibaba.fastjson.JSON;
import com.custom.starters.customwebspringbootstarters.core.exceptions.IErrorCode;
import com.custom.starters.customwebspringbootstarters.core.result.R;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class ErrorHandler {

    public static Mono writeFailedToResponse(ServerHttpResponse response, Integer code, String msg) {
        return writeFailedToResponse(response, R.fail(code, msg));
    }

    public static Mono writeFailedToResponse(ServerHttpResponse response, IErrorCode errorCode) {
        response.setStatusCode(HttpStatus.resolve(errorCode.getCode()));
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        String body = JSON.toJSONString(R.fail(errorCode.getCode(), errorCode.getMsg()));
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer)).doOnError(error -> DataBufferUtils.release(buffer));
    }

    public static Mono writeFailedToResponse(ServerHttpResponse response, R<?> result) {
        return writeFailedToResponse(response, result.getCode(), result.getMsg());
    }
}
