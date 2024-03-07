package com.wio.crm.controller;

import com.wio.crm.model.DashboardData;
import com.wio.crm.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/dashboard-data")
    public ResponseEntity<Map<String, Object>> getDashboardData(Principal principal) {
        // SecurityContext에서 인증 객체를 가져옵니다.
        String username = principal.getName();

        Map<String, Object> data = dashboardService.getDashboardData(username);
        return ResponseEntity.ok(data);
    }
    @GetMapping("/api/dashboard-callCount-data")
    public ResponseEntity<Map<String, Object>> getDashboardConCount(Principal principal) {

        String username = principal.getName();
        Map<String, Object> data = dashboardService.getDashboardCallCount(username);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/api/dashboard-personCount-data")
    public ResponseEntity<Map<String, Object>> getDashboardPersonCount(Principal principal) {

        String username = principal.getName();
        Map<String, Object> data = dashboardService.getDashboardPersonCount(username);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/api/dashboard-month-data")
    public ResponseEntity<Map<String, Object>> getDashboardMonth(Principal principal) {

        String username = principal.getName();
        Map<String, Object> data = dashboardService.getDashboardMonth(username);
        return ResponseEntity.ok(data);
    }



}
