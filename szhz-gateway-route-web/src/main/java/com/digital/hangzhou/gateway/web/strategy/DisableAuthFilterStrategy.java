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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.digital.hangzhou.gateway.web.util.RouteDefinitionUtil.getConsumer;

/**
 * @author lz
 * @date 2023/3/2 15:09
 */
@AuthTypeAnnotation(ApiAuthType.DISABLE)
@NoArgsConstructor
public class DisableAuthFilterStrategy implements AuthFilter {


    @Override
    public List<FilterDefinition> getApiAuthFilters(ReleaseRequest request) {
        List<FilterDefinition> factories = new ArrayList<>(1);
        Map<String,String> param = new HashMap<String, String>(1){{
            put("sources", getConsumer(request.getAppCodes()).toString());
        }};
        factories.add(RouteDefinitionUtil.getAuthFilter(RouteInfoConstant.CONSUMER_PREDICATE_FACTORY, param));
        return factories;
    }
}
