package com.wio.crm.controller;

import com.wio.crm.dto.ShippingPaymentRegisterDTO;
import com.wio.crm.service.ShippingPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 배송비 입금 관리 컨트롤러
 */
@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final ShippingPaymentService shippingPaymentService;

    /**
     * 배송비 입금 등록 화면
     */
    @GetMapping("/register")
    public String register(Model model, Authentication authentication) {
        try {
            model.addAttribute("pageTitle", "배송비 입금 등록");
            model.addAttribute("shippingPaymentRegisterDTO", new ShippingPaymentRegisterDTO());
            
            // 로그인 사용자 정보
            if (authentication != null && authentication.getName() != null) {
                model.addAttribute("currentUser", authentication.getName());
            }
            
            // 오늘 등록된 입금 건수
            Long todayCount = shippingPaymentService.getTodayCount();
            model.addAttribute("todayCount", todayCount);
            
            // 최근 입금 내역 (오늘 등록된 최신 10건)
            List<Map<String, Object>> recentPayments = shippingPaymentService.getRecentPayments(10);
            model.addAttribute("recentPayments", recentPayments);
            
        } catch (Exception e) {
            log.error("입금 등록 화면 로드 오류: {}", e.getMessage(), e);
            model.addAttribute("todayCount", 0L);
            model.addAttribute("recentPayments", List.of());
        }
        
        return "payment/register";
    }

    /**
     * 배송비 입금 등록 처리 (API)
     */
    @PostMapping("/api/register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerPayment(
            @RequestBody ShippingPaymentRegisterDTO registerDTO,
            Authentication authentication) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 로그인 사용자 정보를 등록자로 설정
            if (authentication != null && authentication.getName() != null) {
                registerDTO.setRegistrar(authentication.getName());
                log.info("등록자 설정: {}", authentication.getName());
            }
            
            log.info("배송비 입금 등록 요청: {}", registerDTO);
            
            ShippingPaymentRegisterDTO saved = shippingPaymentService.register(registerDTO);
            
            result.put("success", true);
            result.put("message", "배송비 입금이 성공적으로 등록되었습니다.");
            result.put("data", saved);
            
            log.info("배송비 입금 등록 완료: ID={}", saved.getRegisterId());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("배송비 입금 등록 실패: {}", e.getMessage(), e);
            
            result.put("success", false);
            result.put("message", "배송비 입금 등록 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 고객 정보로 교환 건 조회 API
     */
    @GetMapping("/api/search-exchange-items")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchExchangeItems(
            @RequestParam String customerName,
            @RequestParam String customerPhone) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("교환 건 조회: 고객명={}, 연락처={}", customerName, customerPhone);
            
            List<Map<String, Object>> exchangeItems = shippingPaymentService.findExchangeItemsByCustomer(
                customerName, customerPhone);
            
            result.put("success", true);
            result.put("data", exchangeItems);
            result.put("count", exchangeItems.size());
            
            log.info("교환 건 조회 완료: {} 건", exchangeItems.size());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("교환 건 조회 실패: {}", e.getMessage(), e);
            
            result.put("success", false);
            result.put("message", "교환 건 조회 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", List.of());
            result.put("count", 0);
            
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 입금 내역 목록 조회 API
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPaymentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String mappingStatus) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> searchParams = new HashMap<>();
            searchParams.put("page", page);
            searchParams.put("size", size);
            searchParams.put("keyword", keyword);
            searchParams.put("startDate", startDate);
            searchParams.put("endDate", endDate);
            searchParams.put("brand", brand);
            searchParams.put("mappingStatus", mappingStatus);
            
            log.info("입금 내역 조회 요청: page={}, size={}, keyword={}, startDate={}, endDate={}, brand={}, mappingStatus={}", 
                    page, size, keyword, startDate, endDate, brand, mappingStatus);
            
            Map<String, Object> paymentList = shippingPaymentService.getPaymentList(searchParams);
            log.info("서비스에서 반환된 데이터: {}", paymentList);
            
            result.put("success", true);
            result.putAll(paymentList);
            
            log.info("최종 클라이언트 응답: {}", result);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("입금 내역 조회 실패: {}", e.getMessage(), e);
            
            result.put("success", false);
            result.put("message", "입금 내역 조회 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 입금 내역 삭제 API
     */
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deletePayment(@PathVariable Long id) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("입금 내역 삭제 요청: ID={}", id);
            
            boolean deleted = shippingPaymentService.delete(id);
            
            if (deleted) {
                result.put("success", true);
                result.put("message", "입금 내역이 성공적으로 삭제되었습니다.");
                log.info("입금 내역 삭제 완료: ID={}", id);
            } else {
                result.put("success", false);
                result.put("message", "입금 내역 삭제에 실패했습니다.");
                log.warn("입금 내역 삭제 실패: ID={}", id);
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("입금 내역 삭제 오류: {}", e.getMessage(), e);
            
            result.put("success", false);
            result.put("message", "입금 내역 삭제 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 최근 입금 내역 조회 API
     */
    @GetMapping("/api/recent")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRecentPayments(
            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("최근 입금 내역 조회: 제한={}", limit);
            
            List<Map<String, Object>> recentPayments = shippingPaymentService.getRecentPayments(limit);
            
            result.put("success", true);
            result.put("data", recentPayments);
            result.put("count", recentPayments.size());
            
            log.info("최근 입금 내역 조회 완료: {} 건", recentPayments.size());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("최근 입금 내역 조회 실패: {}", e.getMessage(), e);
            
            result.put("success", false);
            result.put("message", "최근 입금 내역 조회 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", List.of());
            result.put("count", 0);
            
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 오늘 입금 건수 조회 API
     */
    @GetMapping("/api/today-count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTodayCount() {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("오늘 입금 건수 조회");
            
            Long todayCount = shippingPaymentService.getTodayCount();
            
            result.put("success", true);
            result.put("count", todayCount);
            
            log.info("오늘 입금 건수 조회 완료: {} 건", todayCount);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("오늘 입금 건수 조회 실패: {}", e.getMessage(), e);
            
            result.put("success", false);
            result.put("message", "오늘 입금 건수 조회 중 오류가 발생했습니다: " + e.getMessage());
            result.put("count", 0L);
            
            return ResponseEntity.internalServerError().body(result);
        }
    }
} 