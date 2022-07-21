package com.digital.hangzhou.gateway.web.controller;

import com.custom.starters.customwebspringbootstarters.core.result.R;
import com.digital.hangzhou.gateway.common.request.ReleaseAuthRequest;
import com.digital.hangzhou.gateway.common.request.ReleaseRequest;
import com.digital.hangzhou.gateway.web.core.ApiRouteOperate;
import com.digital.hangzhou.gateway.web.core.HtmlRouteOperate;
import com.digital.hangzhou.gateway.web.service.GatewayServiceHandler;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/route/release")
public class ReleaseController {
    @Resource
    private GatewayServiceHandler handler;
    @Resource
    private ApiRouteOperate apiRouteOperate;
    @Resource
    private HtmlRouteOperate htmlRouteOperate;

    @PostMapping("/save")
    public R<Boolean> saveRoute(@RequestBody ReleaseRequest request){
        apiRouteOperate.save(request);
        return R.ok(true);
    }

    @DeleteMapping("/delete")
    public R<Boolean> deleteRoute(@RequestParam String routeId){
        apiRouteOperate.delete(routeId);
        return R.ok(true);
    }

    @GetMapping("/get")
    public R<List<RouteDefinition>> getRoutes(){
      return  R.ok(new ArrayList<>());
    }


    @PostMapping("/refresh")
    public R<Boolean> refreshRoute(@RequestBody ReleaseAuthRequest request){
        handler.refresh(request);
        return R.ok(true);
    }

    @PostMapping("/saveHtml")
    public R<Boolean> saveHtml(@RequestBody ReleaseRequest request){
        htmlRouteOperate.save(request);
        return R.ok(true);
    }

}
