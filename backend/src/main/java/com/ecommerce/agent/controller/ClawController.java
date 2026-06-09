package com.ecommerce.agent.controller;

import com.ecommerce.agent.service.OpenClawService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/claw")
public class ClawController {

    private final OpenClawService openClawService;

    public ClawController(OpenClawService openClawService) {
        this.openClawService = openClawService;
    }

    /** 发起微信扫码绑定 */
    @PostMapping("/bind/start")
    public ResponseEntity<Map<String, Object>> startBind() {
        try {
            Map<String, Object> result = openClawService.startLogin(null);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("绑定失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /** 查询绑定状态 */
    @GetMapping("/bind/status")
    public ResponseEntity<Map<String, Object>> bindStatus() {
        Map<String, Object> result = openClawService.checkStatus(null);
        return ResponseEntity.ok(result);
    }

    /** 取消绑定 */
    @PostMapping("/bind/cancel")
    public ResponseEntity<Map<String, Object>> cancelBind() {
        openClawService.cancelLogin();
        return ResponseEntity.ok(Map.of("success", true));
    }
}
