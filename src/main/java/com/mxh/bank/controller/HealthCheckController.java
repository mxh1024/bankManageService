package com.mxh.bank.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bank-manager")
@Slf4j
public class HealthCheckController {
    /**
     * 服务探活脚本
     */
    @GetMapping("/health")
    public void health() {

    }
}
