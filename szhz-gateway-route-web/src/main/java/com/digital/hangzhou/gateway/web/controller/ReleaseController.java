package com.digital.hangzhou.gateway.web.controller;

import com.custom.starters.customwebspringbootstarters.core.result.R;
import com.digital.hangzhou.gateway.common.request.ReleaseRequest;
import com.digital.hangzhou.gateway.web.service.GatewayServiceHandler;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/route/release")
public class ReleaseController {
    @Resource
    private GatewayServiceHandler handler;

    @PostMapping("/save")
    public R<Boolean> saveRoute(@RequestBody ReleaseRequest request){
        handler.save(request);
        return R.ok(true);
    }

    @DeleteMapping("/delete")
    public R<Boolean> deleteRoute(@RequestParam String routeId){
        handler.delete(routeId);
        return R.ok(true);
    }

    @GetMapping("/get")
    public R<List<RouteDefinition>> getRoutes(){
      return  null;
    }
}
