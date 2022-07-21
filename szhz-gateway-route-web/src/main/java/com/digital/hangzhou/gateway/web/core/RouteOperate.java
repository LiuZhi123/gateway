package com.digital.hangzhou.gateway.web.core;

import com.digital.hangzhou.gateway.common.request.ReleaseRequest;


public interface RouteOperate {
    /**
     * 增加路由的方法
     */
    void save(ReleaseRequest request);

    /**
     * 删除路由的方法
     */
    void delete(String routeId);

}
