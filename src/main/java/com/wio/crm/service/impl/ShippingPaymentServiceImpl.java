package com.wio.crm.service.impl;

import com.wio.crm.dto.ShippingPaymentRegisterDTO;
import com.wio.crm.mapper.ShippingPaymentMapper;
import com.wio.crm.service.ShippingPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 배송비 입금 서비스 구현체
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ShippingPaymentServiceImpl implements ShippingPaymentService {

    private final ShippingPaymentMapper shippingPaymentMapper;

    @Override
    public ShippingPaymentRegisterDTO register(ShippingPaymentRegisterDTO registerDTO) {
        try {
            // 등록자 설정 (현재는 기본값, 추후 세션에서 가져오도록 수정)
            registerDTO.setRegistrar("SYSTEM");
            registerDTO.setRegisterDate(LocalDateTime.now());
            
            // DB에 실제 저장
            shippingPaymentMapper.insertPayment(registerDTO);
            
            log.info("배송비 입금 등록 완료: ID={}, 고객명={}, 금액={}", 
                    registerDTO.getRegisterId(), registerDTO.getCustomerName(), registerDTO.getAmount());
            
            return registerDTO;
            
        } catch (Exception e) {
            log.error("배송비 입금 등록 실패: {}", e.getMessage(), e);
            throw new RuntimeException("배송비 입금 등록에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> findExchangeItemsByCustomer(String customerName, String customerPhone) {
        try {
            // 임시 구현 - 실제로는 데이터베이스에서 조회
            List<Map<String, Object>> exchangeItems = new ArrayList<>();
            
            // 샘플 데이터
            Map<String, Object> item1 = new HashMap<>();
            item1.put("id", 1L);
            item1.put("orderNumber", "ORD-2025-001");
            item1.put("customerName", customerName);
            item1.put("customerPhone", customerPhone);
            item1.put("productName", "테스트 상품1");
            item1.put("returnTypeCode", "EXCHANGE");
            item1.put("returnStatus", "회수완료");
            item1.put("shippingFee", 3000);
            exchangeItems.add(item1);
            
            Map<String, Object> item2 = new HashMap<>();
            item2.put("id", 2L);
            item2.put("orderNumber", "ORD-2025-002");
            item2.put("customerName", customerName);
            item2.put("customerPhone", customerPhone);
            item2.put("productName", "테스트 상품2");
            item2.put("returnTypeCode", "EXCHANGE");
            item2.put("returnStatus", "물류확인");
            item2.put("shippingFee", 2500);
            exchangeItems.add(item2);
            
            log.info("교환 건 조회 완료: {} 건", exchangeItems.size());
            return exchangeItems;
            
        } catch (Exception e) {
            log.error("교환 건 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("교환 건 조회에 실패했습니다.", e);
        }
    }

    @Override
    public Map<String, Object> getPaymentList(Map<String, Object> searchParams) {
        try {
            log.info("입금 내역 조회 시작: {}", searchParams);
            
            // 실제 데이터베이스에서 조회
            List<Map<String, Object>> paymentList = shippingPaymentMapper.selectPaymentList(searchParams);
            Long totalCount = shippingPaymentMapper.selectPaymentCount(searchParams);
            
            // 페이징 정보 계산
            int page = (Integer) searchParams.getOrDefault("page", 0);
            int size = (Integer) searchParams.getOrDefault("size", 20);
            int totalPages = (int) Math.ceil((double) totalCount / size);
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", paymentList);
            result.put("totalElements", totalCount);
            result.put("totalPages", totalPages);
            result.put("currentPage", page);
            result.put("size", size);
            result.put("hasNext", page < totalPages - 1);
            result.put("hasPrev", page > 0);
            result.put("numberOfElements", paymentList.size());
            
            log.info("입금 내역 조회 완료: {} 건 (총 {} 건, 페이지 {}/{})", 
                    paymentList.size(), totalCount, page + 1, totalPages);
            
            return result;
            
        } catch (Exception e) {
            log.error("입금 내역 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("입금 내역 조회에 실패했습니다.", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            // 임시 구현 - 실제로는 데이터베이스에서 삭제
            log.info("입금 내역 삭제: ID={}", id);
            return true;
            
        } catch (Exception e) {
            log.error("입금 내역 삭제 실패: ID={}, 오류={}", id, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public ShippingPaymentRegisterDTO findById(Long id) {
        try {
            // 임시 구현 - 실제로는 데이터베이스에서 조회
            ShippingPaymentRegisterDTO dto = new ShippingPaymentRegisterDTO();
            dto.setRegisterId(id);
            dto.setCustomerName("홍길동");
            dto.setCustomerPhone("010-1234-5678");
            dto.setAmount(3000L);
            dto.setRegisterDate(LocalDateTime.now());
            dto.setNotes("교환 배송비");
            
            return dto;
            
        } catch (Exception e) {
            log.error("입금 내역 조회 실패: ID={}, 오류={}", id, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public ShippingPaymentRegisterDTO update(ShippingPaymentRegisterDTO registerDTO) {
        try {
            // 임시 구현 - 실제로는 데이터베이스에서 수정
            log.info("입금 내역 수정: {}", registerDTO);
            return registerDTO;
            
        } catch (Exception e) {
            log.error("입금 내역 수정 실패: {}", e.getMessage(), e);
            throw new RuntimeException("입금 내역 수정에 실패했습니다.", e);
        }
    }

    @Override
    public boolean mapPaymentToExchange(Long paymentId, Long exchangeId) {
        try {
            // 임시 구현 - 실제로는 매핑 테이블에 저장
            log.info("입금-교환 매핑: 입금ID={}, 교환ID={}", paymentId, exchangeId);
            return true;
            
        } catch (Exception e) {
            log.error("입금-교환 매핑 실패: 입금ID={}, 교환ID={}, 오류={}", paymentId, exchangeId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean unmapPayment(Long paymentId) {
        try {
            // 임시 구현 - 실제로는 매핑 해제
            log.info("입금 매핑 해제: 입금ID={}", paymentId);
            return true;
            
        } catch (Exception e) {
            log.error("입금 매핑 해제 실패: 입금ID={}, 오류={}", paymentId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Long getTodayCount() {
        try {
            LocalDate today = LocalDate.now();
            Long todayCount = shippingPaymentMapper.selectTodayCount(today);
            
            log.info("오늘 입금 건수 조회 완료: {} 건", todayCount);
            return todayCount != null ? todayCount : 0L;
            
        } catch (Exception e) {
            log.error("오늘 입금 건수 조회 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    public List<Map<String, Object>> getRecentPayments(int limit) {
        try {
            List<Map<String, Object>> recentPayments = shippingPaymentMapper.selectRecentPayments(limit);
            
            log.info("최근 입금 내역 조회 완료: {} 건 (제한: {})", recentPayments.size(), limit);
            return recentPayments != null ? recentPayments : new ArrayList<>();
            
        } catch (Exception e) {
            log.error("최근 입금 내역 조회 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
} 