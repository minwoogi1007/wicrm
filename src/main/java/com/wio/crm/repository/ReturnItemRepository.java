package com.wio.crm.repository;

import com.wio.crm.model.ReturnItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 교환/반품 레포지토리 - Oracle 11g 호환
 * 기본 JPA 기능 + 간단한 쿼리만 유지
 */
@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {

    // ==================== 기본 검색 메서드들 ====================
    
    /**
     * 고객명으로 검색
     */
    List<ReturnItem> findByCustomerNameContaining(String customerName);
    
    /**
     * 고객 전화번호로 검색
     */
    List<ReturnItem> findByCustomerPhone(String customerPhone);
    
    /**
     * 주문번호로 검색
     */
    List<ReturnItem> findByOrderNumber(String orderNumber);
    
    /**
     * 교환/반품 유형별 조회
     */
    List<ReturnItem> findByReturnTypeCode(String returnTypeCode);
    
    /**
     * 결제 상태별 조회 (페이징)
     */
    Page<ReturnItem> findByPaymentStatus(String paymentStatus, Pageable pageable);
    
    /**
     * 완료 상태별 조회 (페이징)
     */
    Page<ReturnItem> findByIsCompleted(Boolean isCompleted, Pageable pageable);
    
    /**
     * 교환/반품 타입 IN + 출고일 NOT NULL (페이징)
     */
    Page<ReturnItem> findByReturnTypeCodeInAndShippingDateIsNotNull(List<String> returnTypes, Pageable pageable);
    
    /**
     * 교환/반품 타입 IN + 출고일 NULL (페이징) 
     */
    Page<ReturnItem> findByReturnTypeCodeInAndShippingDateIsNull(List<String> returnTypes, Pageable pageable);
    
    /**
     * 교환/반품 타입 IN + 환불일 NOT NULL (페이징)
     */
    Page<ReturnItem> findByReturnTypeCodeInAndRefundDateIsNotNull(List<String> returnTypes, Pageable pageable);
    
    /**
     * 교환/반품 타입 IN + 환불일 NULL (페이징)
     */
    Page<ReturnItem> findByReturnTypeCodeInAndRefundDateIsNull(List<String> returnTypes, Pageable pageable);
    
    // ==================== 간단한 통계 쿼리들 ====================
    
    /**
     * 전체 건수 (간단한 쿼리)
     */
    @Query("SELECT COUNT(r) FROM ReturnItem r")
    long countAllItems();
    
    /**
     * 특정 날짜 범위 내 데이터 조회 (JPA 쿼리)
     */
    @Query("SELECT r FROM ReturnItem r WHERE r.createDate BETWEEN :startDate AND :endDate ORDER BY r.createDate DESC")
    List<ReturnItem> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // ==================== Oracle 11g 호환 네이티브 쿼리 메서드들 ====================
    
    /**
     * 🔥 Oracle ROWNUM 기반 페이징 (전체 조회)
     */
    @Query(value = "SELECT * FROM (SELECT ROWNUM rn, sub.* FROM (SELECT * FROM TB_RETURN_ITEM ORDER BY RETURN_ID DESC) sub) WHERE rn > ?1 AND rn <= ?2", nativeQuery = true)
    List<ReturnItem> findAllWithPagination(int startRow, int endRow);

    /**
     * 🔥 Oracle 호환 복합 검색 (키워드 + 날짜범위)
     */
    @Query(value = "SELECT * FROM (SELECT ROWNUM rn, sub.* FROM (SELECT * FROM TB_RETURN_ITEM WHERE " +
            "(:keyword IS NULL OR UPPER(ORDER_NUMBER) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(CUSTOMER_NAME) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(CUSTOMER_PHONE) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(SITE_NAME) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(ORDER_ITEM_CODE) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(PRODUCT_COLOR) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(PRODUCT_SIZE) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(TRACKING_NUMBER) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(RETURN_REASON) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(PROCESSOR) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(REMARKS) LIKE UPPER('%' || :keyword || '%')) " +
            "AND (:startDate IS NULL OR CS_RECEIVED_DATE >= TO_DATE(:startDate, 'YYYY-MM-DD')) " +
            "AND (:endDate IS NULL OR CS_RECEIVED_DATE <= TO_DATE(:endDate, 'YYYY-MM-DD')) " +
            "ORDER BY RETURN_ID DESC) sub WHERE ROWNUM <= :endRow) WHERE rn > :startRow", nativeQuery = true)
    List<ReturnItem> findBySearchCriteria(@Param("keyword") String keyword,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate,
                                          @Param("startRow") int startRow,
                                          @Param("endRow") int endRow);

    /**
     * 🔥 Oracle 호환 복합 검색 카운트
     */
    @Query(value = "SELECT COUNT(*) FROM TB_RETURN_ITEM WHERE " +
            "(:keyword IS NULL OR UPPER(ORDER_NUMBER) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(CUSTOMER_NAME) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(CUSTOMER_PHONE) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(SITE_NAME) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(ORDER_ITEM_CODE) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(PRODUCT_COLOR) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(PRODUCT_SIZE) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(TRACKING_NUMBER) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(RETURN_REASON) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(PROCESSOR) LIKE UPPER('%' || :keyword || '%') OR " +
            "UPPER(REMARKS) LIKE UPPER('%' || :keyword || '%')) " +
            "AND (:startDate IS NULL OR CS_RECEIVED_DATE >= TO_DATE(:startDate, 'YYYY-MM-DD')) " +
            "AND (:endDate IS NULL OR CS_RECEIVED_DATE <= TO_DATE(:endDate, 'YYYY-MM-DD'))", nativeQuery = true)
    long countBySearchCriteria(@Param("keyword") String keyword,
                               @Param("startDate") String startDate,
                               @Param("endDate") String endDate);
    
    // ==================== 🎯 카드 필터링 전용 네이티브 쿼리 메서드들 ====================
    
    /**
     * 📦 회수완료 조회 (Oracle ROWNUM 페이징) - 전체완료 제외
     */
    @Query(value = "SELECT * FROM (SELECT ROWNUM rn, sub.* FROM (SELECT * FROM TB_RETURN_ITEM WHERE COLLECTION_COMPLETED_DATE IS NOT NULL AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL) ORDER BY RETURN_ID DESC) sub) WHERE rn > ?1 AND rn <= ?2", nativeQuery = true)
    List<ReturnItem> findByCollectionCompletedDateIsNotNull(int startRow, int endRow);
    
    /**
     * 📦 회수완료 개수 - 전체완료 제외
     */
    @Query(value = "SELECT COUNT(*) FROM TB_RETURN_ITEM WHERE COLLECTION_COMPLETED_DATE IS NOT NULL AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL)", nativeQuery = true)
    Long countByCollectionCompletedDateIsNotNull();
    
    /**
     * 📦 회수미완료 조회 (Oracle ROWNUM 페이징) - 전체완료 제외
     */
    @Query(value = "SELECT * FROM (SELECT ROWNUM rn, sub.* FROM (SELECT * FROM TB_RETURN_ITEM WHERE COLLECTION_COMPLETED_DATE IS NULL AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL) ORDER BY RETURN_ID DESC) sub) WHERE rn > ?1 AND rn <= ?2", nativeQuery = true)
    List<ReturnItem> findByCollectionCompletedDateIsNull(int startRow, int endRow);
    
    /**
     * 📦 회수미완료 개수 - 전체완료 제외
     */
    @Query(value = "SELECT COUNT(*) FROM TB_RETURN_ITEM WHERE COLLECTION_COMPLETED_DATE IS NULL AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL)", nativeQuery = true)
    Long countByCollectionCompletedDateIsNull();
    
    /**
     * 🚛 물류확인완료 조회 (Oracle ROWNUM 페이징) - 전체완료 제외
     */
    @Query(value = "SELECT * FROM (SELECT ROWNUM rn, sub.* FROM (SELECT * FROM TB_RETURN_ITEM WHERE LOGISTICS_CONFIRMED_DATE IS NOT NULL AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL) ORDER BY RETURN_ID DESC) sub) WHERE rn > ?1 AND rn <= ?2", nativeQuery = true)
    List<ReturnItem> findByLogisticsConfirmedDateIsNotNull(int startRow, int endRow);
    
    /**
     * 🚛 물류확인완료 개수 - 전체완료 제외
     */
    @Query(value = "SELECT COUNT(*) FROM TB_RETURN_ITEM WHERE LOGISTICS_CONFIRMED_DATE IS NOT NULL AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL)", nativeQuery = true)
    Long countByLogisticsConfirmedDateIsNotNull();
    
    /**
     * 🚛 물류확인미완료 조회 (Oracle ROWNUM 페이징) - 전체완료 제외
     */
    @Query(value = "SELECT * FROM (SELECT ROWNUM rn, sub.* FROM (SELECT * FROM TB_RETURN_ITEM WHERE LOGISTICS_CONFIRMED_DATE IS NULL AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL) ORDER BY RETURN_ID DESC) sub) WHERE rn > ?1 AND rn <= ?2", nativeQuery = true)
    List<ReturnItem> findByLogisticsConfirmedDateIsNull(int startRow, int endRow);
    
    /**
     * 🚛 물류확인미완료 개수 - 전체완료 제외
     */
    @Query(value = "SELECT COUNT(*) FROM TB_RETURN_ITEM WHERE LOGISTICS_CONFIRMED_DATE IS NULL AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL)", nativeQuery = true)
    Long countByLogisticsConfirmedDateIsNull();
    
    /**
     * 📤 교환 출고완료 조회 (RETURN_TYPE_CODE가 교환이고 출고일자 있음) (Oracle ROWNUM 페이징) - 전체완료 제외
     */
    @Query(value = "SELECT * FROM (SELECT ROWNUM rn, sub.* FROM (SELECT * FROM TB_RETURN_ITEM WHERE RETURN_TYPE_CODE IN ('PARTIAL_EXCHANGE', 'FULL_EXCHANGE', 'EXCHANGE') AND SHIPPING_DATE IS NOT NULL AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL) ORDER BY RETURN_ID DESC) sub) WHERE rn > ?1 AND rn <= ?2", nativeQuery = true)
    List<ReturnItem> findExchangeShipped(int startRow, int endRow);
    
    /**
     * 📤 교환 출고완료 개수 (RETURN_TYPE_CODE가 교환이고 출고일자 있음) - 전체완료 제외
     */
    @Query(value = "SELECT COUNT(*) FROM TB_RETURN_ITEM WHERE RETURN_TYPE_CODE IN ('PARTIAL_EXCHANGE', 'FULL_EXCHANGE', 'EXCHANGE') AND SHIPPING_DATE IS NOT NULL AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL)", nativeQuery = true)
    Long countExchangeShipped();
    
    /**
     * 📤 교환 출고대기 조회 (RETURN_TYPE_CODE가 교환이고 출고일자 없음) (Oracle ROWNUM 페이징) - 전체완료 제외
     */
    @Query(value = "SELECT * FROM (SELECT ROWNUM rn, sub.* FROM (SELECT * FROM TB_RETURN_ITEM WHERE RETURN_TYPE_CODE IN ('PARTIAL_EXCHANGE', 'FULL_EXCHANGE', 'EXCHANGE') AND SHIPPING_DATE IS NULL AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL) ORDER BY RETURN_ID DESC) sub) WHERE rn > ?1 AND rn <= ?2", nativeQuery = true)
    List<ReturnItem> findExchangeNotShipped(int startRow, int endRow);
    
    /**
     * 📤 교환 출고대기 개수 (RETURN_TYPE_CODE가 교환이고 출고일자 없음) - 전체완료 제외
     */
    @Query(value = "SELECT COUNT(*) FROM TB_RETURN_ITEM WHERE RETURN_TYPE_CODE IN ('PARTIAL_EXCHANGE', 'FULL_EXCHANGE', 'EXCHANGE') AND SHIPPING_DATE IS NULL AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL)", nativeQuery = true)
    Long countExchangeNotShipped();
    
    /**
     * 💰 반품 환불완료 조회 (RETURN_TYPE_CODE가 반품이고 환불일자 있음) (Oracle ROWNUM 페이징) - 전체완료 제외
     */
    @Query(value = "SELECT * FROM (SELECT ROWNUM rn, sub.* FROM (SELECT * FROM TB_RETURN_ITEM WHERE RETURN_TYPE_CODE IN ('FULL_RETURN', 'PARTIAL_RETURN') AND REFUND_DATE IS NOT NULL AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL) ORDER BY RETURN_ID DESC) sub) WHERE rn > ?1 AND rn <= ?2", nativeQuery = true)
    List<ReturnItem> findReturnRefunded(int startRow, int endRow);
    
    /**
     * 💰 반품 환불완료 개수 (RETURN_TYPE_CODE가 반품이고 환불일자 있음) - 전체완료 제외
     */
    @Query(value = "SELECT COUNT(*) FROM TB_RETURN_ITEM WHERE RETURN_TYPE_CODE IN ('FULL_RETURN', 'PARTIAL_RETURN') AND REFUND_DATE IS NOT NULL AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL)", nativeQuery = true)
    Long countReturnRefunded();
    
    /**
     * 💰 반품 환불대기 조회 (RETURN_TYPE_CODE가 반품이고 환불일자 없음) (Oracle ROWNUM 페이징) - 전체완료 제외
     */
    @Query(value = "SELECT * FROM (SELECT ROWNUM rn, sub.* FROM (SELECT * FROM TB_RETURN_ITEM WHERE RETURN_TYPE_CODE IN ('FULL_RETURN', 'PARTIAL_RETURN') AND REFUND_DATE IS NULL AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL) ORDER BY RETURN_ID DESC) sub) WHERE rn > ?1 AND rn <= ?2", nativeQuery = true)
    List<ReturnItem> findReturnNotRefunded(int startRow, int endRow);
    
    /**
     * 💰 반품 환불대기 개수 (RETURN_TYPE_CODE가 반품이고 환불일자 없음) - 전체완료 제외
     */
    @Query(value = "SELECT COUNT(*) FROM TB_RETURN_ITEM WHERE RETURN_TYPE_CODE IN ('FULL_RETURN', 'PARTIAL_RETURN') AND REFUND_DATE IS NULL AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL)", nativeQuery = true)
    Long countReturnNotRefunded();
    
    /**
     * 💳 배송비 입금대기 조회 (Oracle ROWNUM 페이징) - 전체완료 제외
     */
    @Query(value = "SELECT * FROM (SELECT ROWNUM rn, sub.* FROM (SELECT * FROM TB_RETURN_ITEM WHERE SHIPPING_FEE = '입금예정' AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL) ORDER BY RETURN_ID DESC) sub) WHERE rn > ?1 AND rn <= ?2", nativeQuery = true)
    List<ReturnItem> findExchangePaymentPending(int startRow, int endRow);
    
    /**
     * 💳 배송비 입금대기 개수 (SHIPPING_FEE = '입금예정') - 전체완료 제외
     */
    @Query(value = "SELECT COUNT(*) FROM TB_RETURN_ITEM WHERE SHIPPING_FEE = '입금예정' AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL)", nativeQuery = true)
    Long countExchangePaymentPending();
    
    /**
     * 💳 배송비 입금완료 조회 (Oracle ROWNUM 페이징) - 전체완료 제외
     */
    @Query(value = "SELECT * FROM (SELECT ROWNUM rn, sub.* FROM (SELECT * FROM TB_RETURN_ITEM WHERE SHIPPING_FEE = '입금완료' AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL) ORDER BY RETURN_ID DESC) sub) WHERE rn > ?1 AND rn <= ?2", nativeQuery = true)
    List<ReturnItem> findExchangePaymentCompleted(int startRow, int endRow);
    
    /**
     * 💳 배송비 입금완료 개수 - 전체완료 제외
     */
    @Query(value = "SELECT COUNT(*) FROM TB_RETURN_ITEM WHERE SHIPPING_FEE = '입금완료' AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL)", nativeQuery = true)
    Long countExchangePaymentCompleted();
    
    /**
     * ✅ 완료 조회 (Oracle ROWNUM 페이징)
     */
    @Query(value = "SELECT * FROM (SELECT ROWNUM rn, sub.* FROM (SELECT * FROM TB_RETURN_ITEM WHERE IS_COMPLETED = 1 ORDER BY RETURN_ID DESC) sub) WHERE rn > ?1 AND rn <= ?2", nativeQuery = true)
    List<ReturnItem> findByIsCompletedTrue(int startRow, int endRow);
    
    /**
     * ✅ 완료 개수
     */
    @Query(value = "SELECT COUNT(*) FROM TB_RETURN_ITEM WHERE IS_COMPLETED = 1", nativeQuery = true)
    Long countByIsCompletedTrue();
    
    /**
     * ❌ 미완료 조회 (Oracle ROWNUM 페이징)
     */
    @Query(value = "SELECT * FROM (SELECT ROWNUM rn, sub.* FROM (SELECT * FROM TB_RETURN_ITEM WHERE IS_COMPLETED = 0 OR IS_COMPLETED IS NULL ORDER BY RETURN_ID DESC) sub) WHERE rn > ?1 AND rn <= ?2", nativeQuery = true)
    List<ReturnItem> findByIsCompletedFalseOrNull(int startRow, int endRow);
    
    /**
     * ❌ 미완료 개수
     */
    @Query(value = "SELECT COUNT(*) FROM TB_RETURN_ITEM WHERE IS_COMPLETED = 0 OR IS_COMPLETED IS NULL", nativeQuery = true)
    Long countByIsCompletedFalseOrNull();
    
    /**
     * 🎯 오늘 등록 건수 (CS_RECEIVED_DATE 기준)
     */
    @Query(value = "SELECT COUNT(*) FROM TB_RETURN_ITEM WHERE TRUNC(CS_RECEIVED_DATE) = TRUNC(SYSDATE)", nativeQuery = true)
    Long countTodayItems();
    
    /**
     * 🎯 어제까지 등록 건수 (CS_RECEIVED_DATE 기준)
     */
    @Query(value = "SELECT COUNT(*) FROM TB_RETURN_ITEM WHERE TRUNC(CS_RECEIVED_DATE) < TRUNC(SYSDATE)", nativeQuery = true)
    Long countUntilYesterdayItems();
    
    /**
     * 🎯 배송비 숫자 합계 (텍스트 제외)
     */
    @Query(value = "SELECT NVL(SUM(CASE WHEN REGEXP_LIKE(SHIPPING_FEE, '^[0-9]+$') THEN TO_NUMBER(SHIPPING_FEE) ELSE 0 END), 0) FROM TB_RETURN_ITEM", nativeQuery = true)
    Long getTotalNumericShippingFee();
    
    /**
     * 🚨 10일 경과 미완료 건수 (Sample에서 통합) - CREATE_DATE 기준
     */
    @Query(value = "SELECT COUNT(*) FROM TB_RETURN_ITEM WHERE CREATE_DATE < :tenDaysAgo AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL)", nativeQuery = true)
    long countByCreateDateBeforeAndIsCompletedFalseOrNull(@Param("tenDaysAgo") java.time.LocalDateTime tenDaysAgo);
    
    // ==================== 🚨 처리기간 임박 필터 - 접수일 기준 10일 이상 미완료 데이터 조회 ====================
    
    /**
     * 🚨 접수일 기준 10일 이상 미완료 건수 조회 (성능 최적화)
     */
    @Query(value = "SELECT COUNT(*) FROM TB_RETURN_ITEM " +
           "WHERE CS_RECEIVED_DATE < :tenDaysAgo AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL)", 
           nativeQuery = true)
    long countOverdueTenDays(@Param("tenDaysAgo") LocalDateTime tenDaysAgo);
    
    /**
     * 🚨 접수일 기준 10일 이상 미완료 데이터 페이징 조회 (Oracle ROWNUM 페이징)
     */
    @Query(value = "SELECT * FROM (SELECT ROWNUM rn, sub.* FROM (SELECT * FROM TB_RETURN_ITEM " +
           "WHERE CS_RECEIVED_DATE < :tenDaysAgo AND (IS_COMPLETED = 0 OR IS_COMPLETED IS NULL) " +
           "ORDER BY CS_RECEIVED_DATE ASC) sub) WHERE rn > :startRow AND rn <= :endRow", 
           nativeQuery = true)
    List<ReturnItem> findOverdueTenDays(@Param("tenDaysAgo") LocalDateTime tenDaysAgo, 
                                        @Param("startRow") int startRow, 
                                        @Param("endRow") int endRow);
} 