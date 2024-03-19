package com.wio.crm.controller;

import com.wio.crm.model.DashboardData;
import com.wio.crm.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
        dashboardService.printUserDetails();
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

    @GetMapping("/empl/dashboard-employee")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public  ResponseEntity<Map<String, Object>> getEmployeeList() {


        Map<String, Object> data = dashboardService.getEmployeeList();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/api/dashboard-daily-data")
    public ResponseEntity<Map<String, Object>> getDailyAve(Principal principal) {

        String username = principal.getName();
        Map<String, Object> data = dashboardService.getDailyAve(username);
        return ResponseEntity.ok(data);
    }
    @GetMapping("/api/dashboard-weekly-data")
    public ResponseEntity<Map<String, Object>> getWeeklySum(Principal principal) {

        String username = principal.getName();
        Map<String, Object> data = dashboardService.getWeeklySum(username);
        return ResponseEntity.ok(data);
    }
    @GetMapping("/api/dashboard-monthly-data")
    public ResponseEntity<Map<String, Object>> getMonthlySum(Principal principal) {

        String username = principal.getName();
        Map<String, Object> data = dashboardService.getMonthlySum(username);
        return ResponseEntity.ok(data);
    }
}