package com.digital.hangzhou.gateway.web.strategy;

import com.digital.hangzhou.gateway.common.annotion.AuthTypeAnnotation;
import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import com.digital.hangzhou.gateway.common.enums.ApiAuthType;
import com.digital.hangzhou.gateway.common.request.ReleaseRequest;
import com.digital.hangzhou.gateway.web.factory.AuthFilter;
import com.digital.hangzhou.gateway.web.util.RouteDefinitionUtil;
import org.springframework.cloud.gateway.filter.FilterDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lz
 * @date 2023/3/8 14:32
 */
@AuthTypeAnnotation(ApiAuthType.SIGN)
public class SignAuthFilterStrategy implements AuthFilter {

    @Override
    public List<FilterDefinition> getApiAuthFilters(ReleaseRequest request) {
        List<FilterDefinition> factories = new ArrayList<>(1);
        factories.add(RouteDefinitionUtil.getAuthFilter(RouteInfoConstant.SIGN_FACTORY, null));
        return factories;
    }
}
