package com.digital.hangzhou.gateway.web.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-1)
@Slf4j
public class GlobalRouteExceptionHandler implements ErrorWebExceptionHandler {
    @Override
    public Mono handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        ErrorMessage errorMessage = ErrorMessage.GATEWAY_CENTER_ERROR;
        if (ex instanceof ResponseStatusException) {
            HttpStatus status = ((ResponseStatusException) ex).getStatus();
            switch (status) {
                case NOT_FOUND:
                    errorMessage = ErrorMessage.GATEWAY_CENTER_NOT_FOUND;
                    break;
                case INTERNAL_SERVER_ERROR:
                    errorMessage = ErrorMessage.GATEWAY_CENTER_ERROR;
                    break;
                case METHOD_NOT_ALLOWED:
                    errorMessage = ErrorMessage.METHOD_NOT_ALLOWED;
                    break;
                case BAD_REQUEST:
                    errorMessage = ErrorMessage.BAD_REQUEST;
                    break;
                case GATEWAY_TIMEOUT:
                    errorMessage = ErrorMessage.GATEWAY_TIMEOUT_MESSAGE;
                    break;
                default:
                    errorMessage = ErrorMessage.GATEWAY_CENTER_ERROR;
                    break;
            }
        }
        log.warn("请求异常:", ex);
        return ErrorHandler.writeFailedToResponse(response, errorMessage);
    }
}
