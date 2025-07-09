package com.wio.crm.controller;

import com.wio.crm.dto.UserActionLogDTO;
import com.wio.crm.service.UserActionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;

@RestController
@RequestMapping("/api/log")
public class UserActionLogController {
    
    private final UserActionLogService logService;
    
    @Autowired
    public UserActionLogController(UserActionLogService logService) {
        this.logService = logService;
    }
    
    @PostMapping("/user-action")
    public ResponseEntity<String> logUserAction(
            @RequestBody UserActionLogDTO logDTO,
            HttpServletRequest request,
            Principal principal) {
        
        if (principal != null) {
            logDTO.setUserId(principal.getName());
        }
        
        // IP 주소 설정
        logDTO.setIpAddress(getClientIp(request));
        
        // User-Agent 설정
        logDTO.setUserAgent(request.getHeader("User-Agent"));
        
        logService.saveUserActionLog(logDTO);
        return ResponseEntity.ok("Logged");
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
} 