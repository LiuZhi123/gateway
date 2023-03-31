package com.digital.hangzhou.gateway.web.factory;

import com.digital.hangzhou.gateway.common.enums.ApiAuthType;
import com.digital.hangzhou.gateway.common.request.ReleaseRequest;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;


/**
 * @author lz
 * @date 2023/3/2 11:45
 */
@Component
public class AuthTypeAnnotationFactory {

    @Resource
    private BeanFactory beanFactory;

    public List<FilterDefinition> getFilterDefinitions(ReleaseRequest releaseRequest){
        ApiAuthType apiAuthType = releaseRequest.getAuthType();
        if ( null == apiAuthType || !beanFactory.containsBean(apiAuthType.name())){
            return null;
        }
        return ((AuthFilter)beanFactory.getBean(apiAuthType.name())).getApiAuthFilters(releaseRequest);
    }
}
