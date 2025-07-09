package com.wio.crm.service;

import com.wio.crm.dto.ShippingPaymentRegisterDTO;

import java.util.List;
import java.util.Map;

/**
 * 배송비 입금 서비스 인터페이스
 */
public interface ShippingPaymentService {

    /**
     * 배송비 입금 등록
     */
    ShippingPaymentRegisterDTO register(ShippingPaymentRegisterDTO registerDTO);

    /**
     * 고객 정보로 교환 건 조회
     */
    List<Map<String, Object>> findExchangeItemsByCustomer(String customerName, String customerPhone);

    /**
     * 입금 내역 목록 조회
     */
    Map<String, Object> getPaymentList(Map<String, Object> searchParams);

    /**
     * 입금 내역 삭제
     */
    boolean delete(Long id);

    /**
     * ID로 조회
     */
    ShippingPaymentRegisterDTO findById(Long id);

    /**
     * 수정
     */
    ShippingPaymentRegisterDTO update(ShippingPaymentRegisterDTO registerDTO);

    /**
     * 교환 건과 입금 내역 매핑
     */
    boolean mapPaymentToExchange(Long paymentId, Long exchangeId);

    /**
     * 매핑 해제
     */
    boolean unmapPayment(Long paymentId);

    /**
     * 오늘 등록된 입금 건수 조회
     */
    Long getTodayCount();

    /**
     * 최근 입금 내역 조회
     */
    List<Map<String, Object>> getRecentPayments(int limit);
} 