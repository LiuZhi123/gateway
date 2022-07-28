package com.digital.hangzhou.gateway.web.controller;

import com.custom.starters.customwebspringbootstarters.core.result.R;
import com.digital.hangzhou.gateway.common.request.GlobalRuleRequest;
import com.digital.hangzhou.gateway.common.request.ReleaseAuthRequest;
import com.digital.hangzhou.gateway.common.request.ReleaseRequest;
import com.digital.hangzhou.gateway.web.core.ApiRouteOperate;
import com.digital.hangzhou.gateway.web.core.HtmlRouteOperate;
import com.digital.hangzhou.gateway.web.service.GatewayServiceHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Api(value = "路由注册", tags = {"根据外部请求进行路由的增删改查"})
@RestController
@RequestMapping("/route/release")
public class ReleaseController {
    @Resource
    private GatewayServiceHandler handler;
    @Resource
    private ApiRouteOperate apiRouteOperate;
    @Resource
    private HtmlRouteOperate htmlRouteOperate;

    @ApiOperation(value =  "生成路由并发送通知节点更新")
    @PostMapping("/save")
    public R<Boolean> saveRoute(@Validated @RequestBody ReleaseRequest request){
        apiRouteOperate.save(request);
        return R.ok(true);
    }

    @ApiOperation(value = "删除路由并通知节点更新")
    @DeleteMapping("/delete")
    public R<Boolean> deleteRoute(@NotNull @RequestParam String routeId){
        apiRouteOperate.delete(routeId);
        return R.ok(true);
    }

    @ApiOperation(value = "获取动态路由")
    @GetMapping("/get")
    public R<List<RouteDefinition>> getRoutes(){
      return  R.ok(new ArrayList<>());
    }

    @ApiOperation(value = "刷新路由")
    @PostMapping("/refresh")
    public R<Boolean> refreshRoute(@Validated @RequestBody ReleaseAuthRequest request){
        handler.refresh(request);
        return R.ok(true);
    }

    @ApiOperation("生成界面路由")
    @PostMapping("/saveHtml")
    public R<Boolean> saveHtml(@Validated @RequestBody ReleaseRequest request){
        htmlRouteOperate.save(request);
        return R.ok(true);
    }

    @ApiOperation("系统限流配置")
    @PostMapping("/systemRule")
    public R<Boolean> saveSystemRules(@RequestBody GlobalRuleRequest request){
        handler.saveSystemRules(request);
        return R.ok(true);
    }
}
