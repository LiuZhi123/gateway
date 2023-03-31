package com.digital.hangzhou.gateway.web.factory;

import com.digital.hangzhou.gateway.common.request.ReleaseRequest;
import org.springframework.cloud.gateway.filter.FilterDefinition;

import java.util.List;
import java.util.Map;

/**
 * @author lz
 * @date 2023/3/2 11:48
 */
public interface AuthFilter {

    List<FilterDefinition> getApiAuthFilters(ReleaseRequest request);

}
