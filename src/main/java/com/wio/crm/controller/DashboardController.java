package com.wio.crm.controller;

import com.wio.crm.model.DashboardData;
import com.wio.crm.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Map<String, DashboardData>> getDashboardData() {
        Map<String, DashboardData> data = dashboardService.getDashboardData();
        return ResponseEntity.ok(data);
    }
}
