package com.digital.hangzhou.gateway.web.controller;

import com.custom.starters.customwebspringbootstarters.core.result.R;
import com.digital.hangzhou.gateway.common.request.ReleaseRequest;
import com.digital.hangzhou.gateway.web.component.GatewayServiceHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
}
