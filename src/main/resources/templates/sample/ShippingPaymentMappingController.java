package com.wio.repairsystem.controller;

import com.wio.repairsystem.dto.ShippingPaymentMappingDTO;
import com.wio.repairsystem.model.ShippingPaymentRegister;
import com.wio.repairsystem.repository.ShippingPaymentRegisterRepository;
import com.wio.repairsystem.service.ShippingPaymentMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 배송비 입금 매핑 통합 대시보드 컨트롤러
 * 상담원의 매핑 작업을 지원하는 통합 화면 제공
 */
@Controller
@RequestMapping("/shipping-payment/mapping")
@RequiredArgsConstructor
@Slf4j
public class ShippingPaymentMappingController {
    
    private final ShippingPaymentMappingService mappingService;
    private final ShippingPaymentRegisterRepository paymentRepository;
    
    /**
     * 간단한 테스트 대시보드 (레이아웃 없음)
     */
    @GetMapping("/test-dashboard")
    public String testDashboard() {
        log.info("간단한 테스트 대시보드 접근");
        return "shipping-payment/mapping-dashboard-simple";
    }
    
    /**
     * 통합 매핑 대시보드 메인 화면
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, 
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String success) {
        log.info("통합 매핑 대시보드 접근 - 페이지: {}, 크기: {}", page, size);
        
        try {
            // 대시보드 통계 조회
            ShippingPaymentMappingDTO.DashboardStats stats = mappingService.getDashboardStats();
            
            // 전체 입금 내역 조회 (최신순)
            List<ShippingPaymentRegister> allPayments = 
                    paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "registerDate"));
            
            // 미매핑 입금 내역만 따로 조회 (후보 검색용)
            List<ShippingPaymentRegister> pendingPayments = 
                    paymentRepository.findByMappingStatus("PENDING", Sort.by(Sort.Direction.DESC, "registerDate"));
            
            // 최근 매핑 내역 조회
            List<ShippingPaymentMappingDTO.MappingResult> recentMappings = 
                    mappingService.getRecentMappings(5);
            
            model.addAttribute("stats", stats);
            model.addAttribute("allPayments", allPayments);
            model.addAttribute("pendingPayments", pendingPayments);
            model.addAttribute("recentMappings", recentMappings);
            
            // 성공 메시지 처리
            if ("register".equals(success)) {
                model.addAttribute("successMessage", "✅ 입금 내역이 성공적으로 등록되었습니다!");
            }
            
            return "shipping-payment/mapping-dashboard";
            
        } catch (Exception e) {
            log.error("대시보드 조회 실패: {}", e.getMessage(), e);
            model.addAttribute("error", "대시보드 정보를 불러오는 중 오류가 발생했습니다.");
            return "shipping-payment/mapping-dashboard";
        }
    }
    
    /**
     * 특정 입금 내역에 대한 후보 조회 (AJAX)
     */
    @GetMapping("/api/candidates/{paymentId}")
    @ResponseBody
    public ResponseEntity<List<ShippingPaymentMappingDTO.ReturnItemCandidate>> getCandidates(
            @PathVariable Long paymentId) {
        log.info("입금 내역 {}에 대한 후보 조회 요청", paymentId);
        
        try {
            List<ShippingPaymentMappingDTO.ReturnItemCandidate> candidates = 
                    mappingService.findCandidatesForPayment(paymentId);
            
            log.info("입금 내역 {}에 대한 후보 {} 개 조회 완료", paymentId, candidates.size());
            return ResponseEntity.ok(candidates);
            
        } catch (Exception e) {
            log.error("후보 조회 실패 - 입금ID: {}: {}", paymentId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 매핑 실행 (AJAX)
     */
    @PostMapping("/api/execute")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> executeMapping(
            @RequestBody ShippingPaymentMappingDTO.MappingRequest request) {
        log.info("매핑 실행 요청 - 입금ID: {}, 교환반품ID: {}", 
                request.getPaymentId(), request.getReturnItemId());
        
        try {
            ShippingPaymentMappingDTO.MappingResult result = mappingService.executeMapping(request);
            
            log.info("매핑 실행 완료 - 입금ID: {}, 교환반품ID: {}", 
                    request.getPaymentId(), request.getReturnItemId());
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", result.getResultMessage(),
                    "canShipExchange", result.getCanShipExchange(),
                    "result", result
            ));
            
        } catch (Exception e) {
            log.error("매핑 실행 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "매핑 실행 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 매핑 취소 (AJAX)
     */
    @PostMapping("/api/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelMapping(
            @RequestBody Map<String, Object> request) {
        Long paymentId = Long.valueOf(request.get("paymentId").toString());
        String canceledBy = request.getOrDefault("canceledBy", "상담원").toString();
        String reason = request.getOrDefault("reason", "수동 취소").toString();
        
        log.info("매핑 취소 요청 - 입금ID: {}, 취소자: {}", paymentId, canceledBy);
        
        try {
            boolean success = mappingService.cancelMapping(paymentId, canceledBy, reason);
            
            if (success) {
                log.info("매핑 취소 완료 - 입금ID: {}", paymentId);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "매핑이 취소되었습니다."
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "매핑 취소에 실패했습니다."
                ));
            }
            
        } catch (Exception e) {
            log.error("매핑 취소 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "매핑 취소 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 자동 매핑 실행 (AJAX)
     */
    @PostMapping("/api/auto-mapping")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> executeAutoMapping(
            @RequestParam String executedBy) {
        log.info("자동 매핑 실행 요청 - 실행자: {}", executedBy);
        
        try {
            List<ShippingPaymentMappingDTO.MappingResult> results = 
                    mappingService.executeAutoMapping(executedBy);
            
            log.info("자동 매핑 완료 - 총 {} 건 매핑됨", results.size());
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", String.format("자동 매핑 완료! 총 %d건이 매핑되었습니다.", results.size()),
                    "mappedCount", results.size(),
                    "results", results
            ));
            
        } catch (Exception e) {
            log.error("자동 매핑 실행 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "자동 매핑 실행 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 교환/반품 후보 검색 (AJAX)
     */
    @GetMapping("/api/search-candidates")
    @ResponseBody
    public ResponseEntity<List<ShippingPaymentMappingDTO.ReturnItemCandidate>> searchCandidates(
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Integer amount,
            @RequestParam(required = false) String keyword) {
        log.info("후보 검색 요청 - 고객명: {}, 금액: {}, 키워드: {}", customerName, amount, keyword);
        
        try {
            List<ShippingPaymentMappingDTO.ReturnItemCandidate> candidates = 
                    mappingService.searchReturnItemCandidates(customerName, amount, keyword);
            
            log.info("후보 검색 완료 - {} 개 결과", candidates.size());
            return ResponseEntity.ok(candidates);
            
        } catch (Exception e) {
            log.error("후보 검색 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 대시보드 통계 새로고침 (AJAX)
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<ShippingPaymentMappingDTO.DashboardStats> getStats() {
        log.info("대시보드 통계 새로고침 요청");
        
        try {
            ShippingPaymentMappingDTO.DashboardStats stats = mappingService.getDashboardStats();
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("통계 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 디버깅용 - PAYMENT_STATUS='PENDING' 데이터 개수 확인
     */
    @GetMapping("/api/debug/pending-count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPendingCount() {
        log.info("PENDING 데이터 개수 확인 요청");
        
        try {
            // 입금 내역 중 미매핑 개수
            long pendingPayments = paymentRepository.countByMappingStatus("PENDING");
            
            // 교환/반품 중 PAYMENT_STATUS='PENDING' 개수는 서비스를 통해 조회
            ShippingPaymentMappingDTO.DashboardStats stats = mappingService.getDashboardStats();
            
            return ResponseEntity.ok(Map.of(
                    "pendingPayments", pendingPayments,
                    "totalPayments", stats.getTotalPayments(),
                    "mappedPayments", stats.getMappedPayments(),
                    "message", String.format("미매핑 입금: %d개, 전체 입금: %d개, 매핑완료: %d개", 
                            pendingPayments, stats.getTotalPayments(), stats.getMappedPayments())
            ));
            
        } catch (Exception e) {
            log.error("PENDING 개수 확인 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "error", e.getMessage(),
                    "message", "데이터 확인 중 오류 발생"
            ));
        }
    }
} 