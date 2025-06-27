package com.wio.repairsystem.service;

import com.wio.repairsystem.dto.ShippingPaymentMappingDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 배송비 입금 매핑 서비스
 * 상담원의 매핑 작업을 지원하는 핵심 서비스
 */
public interface ShippingPaymentMappingService {
    
    /**
     * 대시보드 통계 정보 조회
     */
    ShippingPaymentMappingDTO.DashboardStats getDashboardStats();
    
    /**
     * 미매핑 입금 내역 목록 조회
     */
    Page<ShippingPaymentMappingDTO.PaymentInfo> getPendingPayments(Pageable pageable);
    
    /**
     * 특정 입금 내역에 대한 교환/반품 후보 조회
     */
    List<ShippingPaymentMappingDTO.ReturnItemCandidate> findCandidatesForPayment(Long paymentId);
    
    /**
     * 매핑 실행
     */
    ShippingPaymentMappingDTO.MappingResult executeMapping(ShippingPaymentMappingDTO.MappingRequest request);
    
    /**
     * 매핑 취소
     */
    boolean cancelMapping(Long paymentId, String canceledBy, String reason);
    
    /**
     * 최근 매핑 내역 조회
     */
    List<ShippingPaymentMappingDTO.MappingResult> getRecentMappings(int limit);
    
    /**
     * 자동 매핑 실행 (고신뢰도 후보들)
     */
    List<ShippingPaymentMappingDTO.MappingResult> executeAutoMapping(String executedBy);
    
    /**
     * 매핑 가능한 후보 검색
     */
    List<ShippingPaymentMappingDTO.ReturnItemCandidate> searchReturnItemCandidates(
            String customerName, Integer amount, String keyword);
} 