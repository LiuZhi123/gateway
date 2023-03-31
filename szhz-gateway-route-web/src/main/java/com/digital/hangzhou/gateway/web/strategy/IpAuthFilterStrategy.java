package com.digital.hangzhou.gateway.web.strategy;

import com.digital.hangzhou.gateway.common.annotion.AuthTypeAnnotation;
import com.digital.hangzhou.gateway.common.constant.RouteInfoConstant;
import com.digital.hangzhou.gateway.common.enums.ApiAuthType;
import com.digital.hangzhou.gateway.common.request.ReleaseRequest;
import com.digital.hangzhou.gateway.web.factory.AuthFilter;
import com.digital.hangzhou.gateway.web.util.RouteDefinitionUtil;
import lombok.NoArgsConstructor;
import org.springframework.cloud.gateway.filter.FilterDefinition;

import java.util.ArrayList;
import java.util.List;


/**
 * @author lz
 * @date 2023/3/2 15:33
 */
@AuthTypeAnnotation(ApiAuthType.IP)
@NoArgsConstructor
public class IpAuthFilterStrategy implements AuthFilter {


    @Override
    public List<FilterDefinition> getApiAuthFilters(ReleaseRequest request) {
        List<FilterDefinition> factories = new ArrayList<>(1);
        factories.add(RouteDefinitionUtil.getAuthFilter(RouteInfoConstant.WHITE_IP_PREDICATE_FACTORY, null));
        return factories;
    }
}
