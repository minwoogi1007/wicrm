package com.wio.repairsystem.controller;

import com.wio.repairsystem.dto.ShippingPaymentRegisterDTO;
import com.wio.repairsystem.model.ReturnItem;
import com.wio.repairsystem.service.ShippingPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 배송비 입금 관리 컨트롤러
 */
@Controller
@RequestMapping("/shipping-payment")
@RequiredArgsConstructor
@Slf4j
public class ShippingPaymentController {
    
    private final ShippingPaymentService shippingPaymentService;
    
    /**
     * 입금 등록 페이지
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("paymentDTO", new ShippingPaymentRegisterDTO());
        return "shipping-payment/register";
    }
    
    /**
     * 입금 등록 처리
     */
    @PostMapping("/register")
    public String registerPayment(@ModelAttribute ShippingPaymentRegisterDTO dto, 
                                 Authentication authentication,
                                 Model model) {
        try {
            // 등록자 정보 설정
            dto.setRegistrar(authentication.getName());
            
            // 브랜드별 기본 은행 설정
            if (dto.getBankName() == null || dto.getBankName().isEmpty()) {
                dto.setBankName(ShippingPaymentRegisterDTO.getDefaultBankByBrand(dto.getBrand()));
            }
            
            // 등록 처리
            ShippingPaymentRegisterDTO saved = shippingPaymentService.registerPayment(dto);
            
            log.info("배송비 입금 등록 성공: ID={}, 고객명={}", saved.getRegisterId(), saved.getCustomerName());
            
            return "redirect:/shipping-payment/mapping/dashboard?success=register";
            
        } catch (Exception e) {
            log.error("배송비 입금 등록 실패: {}", e.getMessage(), e);
            model.addAttribute("error", "입금 등록 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("paymentDTO", dto);
            return "shipping-payment/register";
        }
    }
    
    /**
     * 입금 관리 목록 페이지 - 새로운 통합 대시보드로 리다이렉트
     */
    @GetMapping("/list")
    public String listPage() {
        log.info("기존 list 페이지 접근 - 새로운 통합 대시보드로 리다이렉트");
        return "redirect:/shipping-payment/mapping/dashboard";
    }
    
    /**
     * 입금 상세 조회 (AJAX)
     */
    @GetMapping("/detail/{paymentId}")
    @ResponseBody
    public ResponseEntity<ShippingPaymentRegisterDTO> getPaymentDetail(@PathVariable Long paymentId) {
        try {
            ShippingPaymentRegisterDTO payment = shippingPaymentService.getPaymentDetail(paymentId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.error("입금 상세 조회 실패: paymentId={}, error={}", paymentId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 매핑 가능한 교환/반품 목록 조회 (AJAX)
     */
    @GetMapping("/mappable-returns")
    @ResponseBody
    public ResponseEntity<List<ReturnItem>> getMappableReturns(@RequestParam String customerName,
                                                              @RequestParam String customerPhone) {
        try {
            List<ReturnItem> returns = shippingPaymentService.getMappableReturns(customerName, customerPhone);
            return ResponseEntity.ok(returns);
        } catch (Exception e) {
            log.error("매핑 가능한 교환/반품 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 수동 매핑 처리 (AJAX)
     */
    @PostMapping("/mapping")
    @ResponseBody
    public ResponseEntity<Map<String, String>> performMapping(@RequestParam Long paymentId,
                                                             @RequestParam Long returnItemId) {
        Map<String, String> result = new HashMap<>();
        
        try {
            shippingPaymentService.performMapping(paymentId, returnItemId);
            
            result.put("status", "success");
            result.put("message", "매핑이 성공적으로 처리되었습니다.");
            
            log.info("수동 매핑 성공: paymentId={}, returnItemId={}", paymentId, returnItemId);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("수동 매핑 실패: paymentId={}, returnItemId={}, error={}", paymentId, returnItemId, e.getMessage());
            
            result.put("status", "error");
            result.put("message", "매핑 처리 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 매핑 해제 (AJAX)
     */
    @PostMapping("/unmapping")
    @ResponseBody
    public ResponseEntity<Map<String, String>> unmapping(@RequestParam Long paymentId) {
        Map<String, String> result = new HashMap<>();
        
        try {
            shippingPaymentService.unmapping(paymentId);
            
            result.put("status", "success");
            result.put("message", "매핑이 성공적으로 해제되었습니다.");
            
            log.info("매핑 해제 성공: paymentId={}", paymentId);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("매핑 해제 실패: paymentId={}, error={}", paymentId, e.getMessage());
            
            result.put("status", "error");
            result.put("message", "매핑 해제 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 입금 내역 삭제
     */
    @PostMapping("/delete/{paymentId}")
    public String deletePayment(@PathVariable Long paymentId) {
        try {
            shippingPaymentService.deletePayment(paymentId);
            
            log.info("입금 내역 삭제 성공: paymentId={}", paymentId);
            
            return "redirect:/shipping-payment/list?success=delete";
            
        } catch (Exception e) {
            log.error("입금 내역 삭제 실패: paymentId={}, error={}", paymentId, e.getMessage());
            
            return "redirect:/shipping-payment/list?error=delete";
        }
    }
    
    /**
     * 브랜드별 기본 은행 정보 조회 (AJAX)
     */
    @GetMapping("/default-bank/{brand}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getDefaultBank(@PathVariable String brand) {
        Map<String, String> result = new HashMap<>();
        
        String bankName = ShippingPaymentRegisterDTO.getDefaultBankByBrand(brand);
        result.put("bankName", bankName);
        
        return ResponseEntity.ok(result);
    }
} 