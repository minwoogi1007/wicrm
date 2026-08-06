package com.wio.crm.controller;

import com.wio.crm.model.AiReport;
import com.wio.crm.model.AiReportSubscription;
import com.wio.crm.service.AiAnalysisService;
import com.wio.crm.service.AiReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * AI 상담 분석 리포트 컨트롤러
 */
@Controller
@RequestMapping("/ai-report")
public class AiReportController {
    
    private static final Logger log = LoggerFactory.getLogger(AiReportController.class);
    
    @Autowired
    private AiReportService aiReportService;
    
    @Autowired
    private AiAnalysisService aiAnalysisService;
    
    /**
     * 업체용 - 내 리포트 목록
     */
    @GetMapping
    public String myReports(HttpSession session, Model model) {
        String custCode = (String) session.getAttribute("CUST_CODE");
        if (custCode == null) {
            return "redirect:/login";
        }
        
        // 구독 상태 확인
        AiReportSubscription subscription = aiReportService.getActiveSubscription(custCode);
        model.addAttribute("subscription", subscription);
        model.addAttribute("hasSubscription", subscription != null);
        
        // 리포트 목록
        if (subscription != null) {
            List<AiReport> reports = aiReportService.getReportsByCustCode(custCode);
            model.addAttribute("reports", reports);
        }
        
        return "ai-report/my-reports";
    }
    
    /**
     * 업체용 - 리포트 상세 보기
     */
    @GetMapping("/view/{id}")
    public String viewReport(@PathVariable("id") Long id, HttpSession session, Model model) {
        String custCode = (String) session.getAttribute("CUST_CODE");
        AiReport report = aiReportService.getReportById(id);
        
        if (report == null || !report.getCustCode().equals(custCode)) {
            return "redirect:/ai-report";
        }
        
        model.addAttribute("report", report);
        return "ai-report/view";
    }
    
    /**
     * 업체용 - 리포트 생성 요청
     */
    @PostMapping("/request")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> requestReport(
            @RequestParam("reportMonth") String reportMonth,
            HttpSession session) {
        
        Map<String, Object> result = new HashMap<>();
        String custCode = (String) session.getAttribute("CUST_CODE");
        
        try {
            AiReport report = aiReportService.requestMonthlyReport(custCode, reportMonth);
            
            // 비동기로 AI 분석 실행 (실제로는 별도 스레드나 배치로 처리)
            new Thread(() -> {
                try {
                    aiReportService.processReport(report.getReportId());
                } catch (Exception e) {
                    log.error("리포트 처리 실패", e);
                }
            }).start();
            
            result.put("success", true);
            result.put("message", "리포트 생성이 요청되었습니다. 처리 완료 후 알림을 보내드립니다.");
            result.put("reportId", report.getReportId());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 업체용 - 구독 신청 페이지
     */
    @GetMapping("/subscribe")
    public String subscribeForm(HttpSession session, Model model) {
        String custCode = (String) session.getAttribute("CUST_CODE");
        
        // 이미 구독 중인지 확인
        AiReportSubscription existing = aiReportService.getActiveSubscription(custCode);
        if (existing != null) {
            model.addAttribute("subscription", existing);
            return "ai-report/subscription-detail";
        }
        
        model.addAttribute("subscription", new AiReportSubscription());
        return "ai-report/subscribe";
    }
    
    /**
     * 업체용 - 구독 신청 처리
     */
    @PostMapping("/subscribe")
    public String subscribe(
            @ModelAttribute AiReportSubscription subscription,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        String custCode = (String) session.getAttribute("CUST_CODE");
        String userId = (String) session.getAttribute("loginUserId");
        
        subscription.setCustCode(custCode);
        subscription.setInEmpno(userId);
        
        try {
            aiReportService.createSubscription(subscription);
            redirectAttributes.addFlashAttribute("success", "구독 신청이 완료되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "구독 신청 실패: " + e.getMessage());
        }
        
        return "redirect:/ai-report";
    }
    
    // ========== 관리자 기능 ==========
    
    /**
     * 관리자 - 구독 관리 목록
     */
    @GetMapping("/admin/subscriptions")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public String adminSubscriptions(Model model) {
        model.addAttribute("subscriptions", aiReportService.getAllSubscriptions());
        model.addAttribute("activeCount", aiReportService.countActiveSubscriptions());
        return "ai-report/admin/subscriptions";
    }
    
    /**
     * 관리자 - 리포트 관리 목록
     */
    @GetMapping("/admin/reports")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public String adminReports(Model model) {
        model.addAttribute("reports", aiReportService.getAllReports());
        
        // 현재 월
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String currentMonth = sdf.format(new Date());
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("monthlyCount", aiReportService.countReportsByMonth(currentMonth));
        
        return "ai-report/admin/reports";
    }
    
    /**
     * 관리자 - AI 연결 테스트
     */
    @GetMapping("/admin/test-ai")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testAiConnection() {
        Map<String, Object> result = aiAnalysisService.testConnection();
        return ResponseEntity.ok(result);
    }
    
    /**
     * 관리자 - 리포트 수동 생성
     */
    @PostMapping("/admin/generate")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateReport(
            @RequestParam("custCode") String custCode,
            @RequestParam("reportMonth") String reportMonth) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            AiReport report = aiReportService.requestMonthlyReport(custCode, reportMonth);
            aiReportService.processReport(report.getReportId());
            
            result.put("success", true);
            result.put("message", "리포트가 생성되었습니다.");
            result.put("reportId", report.getReportId());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 관리자 - 대시보드
     */
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public String adminDashboard(Model model) {
        model.addAttribute("activeSubscriptions", aiReportService.countActiveSubscriptions());
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String currentMonth = sdf.format(new Date());
        model.addAttribute("monthlyReports", aiReportService.countReportsByMonth(currentMonth));
        
        model.addAttribute("recentReports", aiReportService.getAllReports());
        model.addAttribute("subscriptions", aiReportService.getActiveSubscriptions());
        
        return "ai-report/admin/dashboard";
    }
}

