package com.wio.crm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 프로젝트 계획 실시간 모니터링 컨트롤러
 * 로그인 없이 접근 가능한 프로젝트 진행 상황 화면
 */
@Controller
@RequestMapping("/project-plan")
@RequiredArgsConstructor
@Slf4j
public class ProjectPlanController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 프로젝트 계획 모니터링 메인 화면
     * Security 설정에서 허용된 경로로 로그인 없이 접근 가능
     */
    @GetMapping("/monitor")
    public String projectPlanMonitor(Model model) {
        try {
            log.info("📊 프로젝트 계획 모니터링 화면 접속");
            
            // 기본 정보 설정
            model.addAttribute("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            model.addAttribute("refreshInterval", 30); // 30초마다 자동 새로고침
            
            return "project-plan/monitor";
            
        } catch (Exception e) {
            log.error("❌ 프로젝트 계획 모니터링 화면 로드 실패: {}", e.getMessage(), e);
            model.addAttribute("error", "프로젝트 계획을 불러올 수 없습니다.");
            return "error";
        }
    }

    /**
     * SHRIMP tasks.json 데이터 API (실시간 조회)
     */
    @GetMapping("/api/tasks")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTasks() {
        try {
            log.info("🔄 SHRIMP 작업 데이터 API 호출");
            
            Map<String, Object> response = new HashMap<>();
            
            // SHRIMP/tasks.json 파일 읽기
            String tasksContent = readShrimpTasksFile();
            
            if (tasksContent != null) {
                // JSON 파싱
                Map<String, Object> tasksData = objectMapper.readValue(tasksContent, Map.class);
                
                // 통계 계산
                Map<String, Object> statistics = calculateTaskStatistics(tasksData);
                
                response.put("success", true);
                response.put("tasks", tasksData);
                response.put("statistics", statistics);
                response.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
                log.info("✅ SHRIMP 작업 데이터 조회 성공 - 작업 수: {}", statistics.get("totalCount"));
                
            } else {
                response.put("success", false);
                response.put("message", "SHRIMP 작업 파일을 찾을 수 없습니다.");
                response.put("tasks", Map.of("tasks", new Object[0]));
                response.put("statistics", getDefaultStatistics());
                response.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
                log.warn("⚠️ SHRIMP 작업 파일 없음");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ SHRIMP 작업 데이터 조회 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "작업 데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
            errorResponse.put("tasks", Map.of("tasks", new Object[0]));
            errorResponse.put("statistics", getDefaultStatistics());
            errorResponse.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * SHRIMP/tasks.json 파일 읽기
     */
    private String readShrimpTasksFile() {
        try {
            // 프로젝트 루트의 SHRIMP/tasks.json 파일 경로
            Path tasksFilePath = Paths.get("SHRIMP/tasks.json");
            
            if (Files.exists(tasksFilePath)) {
                return Files.readString(tasksFilePath);
            } else {
                log.warn("⚠️ SHRIMP tasks.json 파일 없음: {}", tasksFilePath.toAbsolutePath());
                return null;
            }
            
        } catch (IOException e) {
            log.error("❌ SHRIMP tasks.json 파일 읽기 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 작업 통계 계산
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> calculateTaskStatistics(Map<String, Object> tasksData) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // ArrayList로 받아서 처리
            java.util.List<Object> tasksList = (java.util.List<Object>) tasksData.get("tasks");
            
            if (tasksList == null) {
                return getDefaultStatistics();
            }
            
            int totalCount = tasksList.size();
            int completedCount = 0;
            int inProgressCount = 0;
            int pendingCount = 0;
            
            for (Object taskObj : tasksList) {
                Map<String, Object> task = (Map<String, Object>) taskObj;
                String status = (String) task.get("status");
                
                if ("完了".equals(status) || "已完成".equals(status)) {
                    completedCount++;
                } else if ("進行中".equals(status) || "进行中".equals(status)) {
                    inProgressCount++;
                } else {
                    pendingCount++;
                }
            }
            
            // 진행률 계산
            double completionRate = totalCount > 0 ? (double) completedCount / totalCount * 100 : 0;
            
            stats.put("totalCount", totalCount);
            stats.put("completedCount", completedCount);
            stats.put("inProgressCount", inProgressCount);
            stats.put("pendingCount", pendingCount);
            stats.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
            
            log.info("📊 통계 계산 완료 - 전체: {}, 완료: {}, 진행중: {}, 대기: {}", 
                totalCount, completedCount, inProgressCount, pendingCount);
            
        } catch (Exception e) {
            log.error("❌ 작업 통계 계산 실패: {}", e.getMessage(), e);
            return getDefaultStatistics();
        }
        
        return stats;
    }

    /**
     * 기본 통계 데이터
     */
    private Map<String, Object> getDefaultStatistics() {
        Map<String, Object> defaultStats = new HashMap<>();
        defaultStats.put("totalCount", 0);
        defaultStats.put("completedCount", 0);
        defaultStats.put("inProgressCount", 0);
        defaultStats.put("pendingCount", 0);
        defaultStats.put("completionRate", 0.0);
        return defaultStats;
    }
} 