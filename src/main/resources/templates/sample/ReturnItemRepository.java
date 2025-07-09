package com.wio.repairsystem.repository;

import com.wio.repairsystem.model.ReturnItem;
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
 * 교환/반품 정보 Repository (성능 최적화 버전)
 * 
 * 사용 화면:
 * - templates/exchange/list.html : 목록 조회, 검색, 통계 데이터 제공
 * - templates/exchange/form.html : 등록, 수정 시 데이터 저장
 * - templates/exchange/view.html : 상세 조회 시 데이터 로드
 * 
 * 테이블: TB_RETURN_ITEM
 * 주요 기능: CRUD, 검색, 통계 쿼리
 * 성능 최적화: 인덱스 활용, 쿼리 최적화, 페이징 개선
 */

@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {
    
    // 🚀 성능 최적화된 기본 페이징 조회 (완료/미완료 분리 + 날짜 내림차순)
    @Query(value = "SELECT /*+ INDEX_DESC(tb_return_item IDX_RETURN_ITEM_ID_DESC) */ * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT * FROM tb_return_item " +
           "    ORDER BY " +
           "      CASE WHEN (is_completed = 0 OR is_completed IS NULL) THEN 0 ELSE 1 END ASC, " +
           "      cs_received_date DESC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findAllWithPagination(@Param("startRow") int startRow, @Param("endRow") int endRow);
    
    @Query(value = "SELECT COUNT(*) FROM tb_return_item", nativeQuery = true)
    long countAllItems();

    // 🚀 성능 최적화된 상태별 조회 (인덱스 활용)
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_STATUS_DATE) */ * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT * FROM tb_return_item WHERE return_status_code = :status ORDER BY return_id DESC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findByReturnStatusCodeOptimized(@Param("status") String status, @Param("startRow") int startRow, @Param("endRow") int endRow);
    
    @Query(value = "SELECT COUNT(*) FROM tb_return_item WHERE return_status_code = :status", nativeQuery = true)
    long countByReturnStatusCodeOptimized(@Param("status") String status);

    // 🚀 성능 최적화된 타입별 조회 (인덱스 활용)
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_TYPE_DATE) */ * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT * FROM tb_return_item WHERE return_type_code = :type ORDER BY return_id DESC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findByReturnTypeCodeOptimized(@Param("type") String type, @Param("startRow") int startRow, @Param("endRow") int endRow);
    
    @Query(value = "SELECT COUNT(*) FROM tb_return_item WHERE return_type_code = :type", nativeQuery = true)
    long countByReturnTypeCodeOptimized(@Param("type") String type);

    // 기존 JPA 메서드들 (호환성 유지)
    Page<ReturnItem> findByReturnStatusCode(String status, Pageable pageable);
    Page<ReturnItem> findByReturnTypeCode(String type, Pageable pageable);
    Page<ReturnItem> findByReturnStatusCodeAndReturnTypeCode(String status, String type, Pageable pageable);
    Page<ReturnItem> findByOrderNumberContaining(String orderNumber, Pageable pageable);
    Page<ReturnItem> findByCustomerNameContaining(String customerName, Pageable pageable);
    Page<ReturnItem> findByCustomerPhoneContaining(String customerPhone, Pageable pageable);
    Page<ReturnItem> findByCsReceivedDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    // 🚀 성능 최적화된 통합검색 쿼리 (인덱스 힌트 추가) - IS_COMPLETED 조건 개선
    @Query(value = "SELECT /*+ FIRST_ROWS(20) */ * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT /*+ INDEX_COMBINE(tb_return_item) */ * FROM tb_return_item " +
           "    WHERE (:keyword IS NULL OR " +
           "           (:keyword IS NOT NULL AND (" +
           "               order_number LIKE :keywordPattern OR " +
           "               customer_name LIKE :keywordPattern OR " +
           "               customer_phone LIKE :keywordPattern OR " +
           "               site_name LIKE :keywordPattern OR " +
           "               order_item_code LIKE :keywordPattern OR " +
           "               product_color LIKE :keywordPattern OR " +
           "               product_size LIKE :keywordPattern OR " +
           "               tracking_number LIKE :keywordPattern OR " +
           "               return_reason LIKE :keywordPattern OR " +
           "               return_type_code LIKE :keywordPattern OR " +
           "               (:keyword = 'Y' AND is_completed = 1) OR " +
           "               (:keyword = 'N' AND (is_completed = 0 OR is_completed IS NULL)) OR " +
           "               (:keyword = '완료' AND is_completed = 1) OR " +
           "               (:keyword = '전체완료' AND is_completed = 1) OR " +
           "               (:keyword = '미완료' AND (is_completed = 0 OR is_completed IS NULL)) OR " +
           "               (:keyword = '진행중' AND (is_completed = 0 OR is_completed IS NULL)) OR " +
           "               processor LIKE :keywordPattern OR " +
           "               remarks LIKE :keywordPattern" +
           "           ))" +
           "    ) " +
           "    AND (:startDate IS NULL OR cs_received_date >= :startDate) " +
           "    AND (:endDate IS NULL OR cs_received_date <= :endDate) " +
           "    ORDER BY " +
           "      CASE WHEN (is_completed = 0 OR is_completed IS NULL) THEN 0 ELSE 1 END ASC, " +
           "      cs_received_date DESC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findBySearchCriteriaOptimized(
            @Param("keyword") String keyword,
            @Param("keywordPattern") String keywordPattern,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("startRow") int startRow,
            @Param("endRow") int endRow);
    
    // 🚀 성능 최적화된 검색 카운트 쿼리 - IS_COMPLETED 조건 개선
    @Query(value = "SELECT /*+ INDEX_COMBINE(tb_return_item) */ COUNT(*) FROM tb_return_item " +
           "WHERE (:keyword IS NULL OR " +
           "       (:keyword IS NOT NULL AND (" +
           "           order_number LIKE :keywordPattern OR " +
           "           customer_name LIKE :keywordPattern OR " +
           "           customer_phone LIKE :keywordPattern OR " +
           "           site_name LIKE :keywordPattern OR " +
           "           order_item_code LIKE :keywordPattern OR " +
           "           product_color LIKE :keywordPattern OR " +
           "           product_size LIKE :keywordPattern OR " +
           "           tracking_number LIKE :keywordPattern OR " +
           "           return_reason LIKE :keywordPattern OR " +
           "           return_type_code LIKE :keywordPattern OR " +
           "           (:keyword = 'Y' AND is_completed = 1) OR " +
           "           (:keyword = 'N' AND (is_completed = 0 OR is_completed IS NULL)) OR " +
           "           (:keyword = '완료' AND is_completed = 1) OR " +
           "           (:keyword = '전체완료' AND is_completed = 1) OR " +
           "           (:keyword = '미완료' AND (is_completed = 0 OR is_completed IS NULL)) OR " +
           "           (:keyword = '진행중' AND (is_completed = 0 OR is_completed IS NULL)) OR " +
           "           processor LIKE :keywordPattern OR " +
           "           remarks LIKE :keywordPattern" +
           "       ))" +
           ") " +
           "AND (:startDate IS NULL OR cs_received_date >= :startDate) " +
           "AND (:endDate IS NULL OR cs_received_date <= :endDate)", 
           nativeQuery = true)
    long countBySearchCriteriaOptimized(
            @Param("keyword") String keyword,
            @Param("keywordPattern") String keywordPattern,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 기존 검색 메서드 (호환성 유지) - IS_COMPLETED 조건 개선
    @Query(value = "SELECT * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT * FROM tb_return_item " +
           "    WHERE (:keyword IS NULL OR " +
           "           order_number LIKE '%' || :keyword || '%' OR " +
           "           customer_name LIKE '%' || :keyword || '%' OR " +
           "           customer_phone LIKE '%' || :keyword || '%' OR " +
           "           site_name LIKE '%' || :keyword || '%' OR " +
           "           order_item_code LIKE '%' || :keyword || '%' OR " +
           "           product_color LIKE '%' || :keyword || '%' OR " +
           "           product_size LIKE '%' || :keyword || '%' OR " +
           "           tracking_number LIKE '%' || :keyword || '%' OR " +
           "           return_reason LIKE '%' || :keyword || '%' OR " +
           "           return_type_code LIKE '%' || :keyword || '%' OR " +
           "           CASE return_type_code " +
           "               WHEN 'FULL_RETURN' THEN '전체반품' " +
           "               WHEN 'PARTIAL_RETURN' THEN '부분반품' " +
           "               WHEN 'FULL_EXCHANGE' THEN '전체교환' " +
           "               WHEN 'PARTIAL_EXCHANGE' THEN '부분교환' " +
           "               WHEN 'EXCHANGE' THEN '일반교환' " +
           "               ELSE return_type_code " +
           "           END LIKE '%' || :keyword || '%' OR " +
           "           CASE " +
           "               WHEN is_completed = 1 THEN '완료' " +
           "               WHEN (is_completed = 0 OR is_completed IS NULL) THEN '미완료' " +
           "               ELSE '미완료' " +
           "           END LIKE '%' || :keyword || '%' OR " +
           "           (:keyword = 'Y' AND is_completed = 1) OR " +
           "           (:keyword = 'N' AND (is_completed = 0 OR is_completed IS NULL)) OR " +
           "           (:keyword = '완료' AND is_completed = 1) OR " +
           "           (:keyword = '전체완료' AND is_completed = 1) OR " +
           "           (:keyword = '미완료' AND (is_completed = 0 OR is_completed IS NULL)) OR " +
           "           (:keyword = '진행중' AND (is_completed = 0 OR is_completed IS NULL)) OR " +
           "           processor LIKE '%' || :keyword || '%' OR " +
           "           remarks LIKE '%' || :keyword || '%') " +
           "    AND (:startDate IS NULL OR cs_received_date >= :startDate) " +
           "    AND (:endDate IS NULL OR cs_received_date <= :endDate) " +
           "    ORDER BY " +
           "      CASE WHEN (is_completed = 0 OR is_completed IS NULL) THEN 0 ELSE 1 END ASC, " +
           "      cs_received_date DESC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findBySearchCriteria(
            @Param("keyword") String keyword,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("startRow") int startRow,
            @Param("endRow") int endRow);
    
    // 🚀 성능 최적화된 미완료 항목 조회 (복합 인덱스 활용)
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_COMPLETION) */ * FROM tb_return_item " +
           "WHERE is_completed = 0 ORDER BY return_id DESC",
           nativeQuery = true)
    List<ReturnItem> findByIsCompletedFalseOptimized();
    
    List<ReturnItem> findByIsCompletedFalse();
    
    @Query("SELECT COUNT(r) FROM ReturnItem r WHERE r.returnStatusCode = :status")
    long countByStatus(@Param("status") String status);
    
    @Query("SELECT COUNT(r) FROM ReturnItem r WHERE r.csReceivedDate = :date")
    long countByCsReceivedDate(@Param("date") LocalDate date);
    
    /**
     * 금일 등록 건수 조회
     */
    @Query("SELECT COUNT(r) FROM ReturnItem r WHERE r.csReceivedDate = CURRENT_DATE")
    long countTodayItems();
    
    // 배송비 입금 관련 메서드들 (성능 최적화)
    /**
     * 고객명, 연락처, 배송비 상태로 교환/반품 조회 (자동 매핑용) - 인덱스 활용
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_CUSTOMER_PAY) */ * FROM tb_return_item " +
           "WHERE customer_name = :customerName AND customer_phone = :customerPhone AND payment_status = :paymentStatus",
           nativeQuery = true)
    List<ReturnItem> findByCustomerNameAndCustomerPhoneAndPaymentStatusOptimized(
            @Param("customerName") String customerName, 
            @Param("customerPhone") String customerPhone, 
            @Param("paymentStatus") String paymentStatus);
    
    /**
     * 기존 메서드 (호환성 유지)
     */
    List<ReturnItem> findByCustomerNameAndCustomerPhoneAndPaymentStatus(
            String customerName, String customerPhone, String paymentStatus);
    
    /**
     * 배송비 상태별 조회 (인덱스 활용)
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_PAYMENT_STATUS) */ * FROM tb_return_item " +
           "WHERE payment_status = :paymentStatus ORDER BY return_id DESC",
           nativeQuery = true)
    List<ReturnItem> findByPaymentStatusOptimized(@Param("paymentStatus") String paymentStatus);
    
    List<ReturnItem> findByPaymentStatus(String paymentStatus);
    
    /**
     * 배송비 상태별 조회 (아직 매핑되지 않은 건만) - 매핑 대시보드용
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_PAYMENT_STATUS) */ * FROM tb_return_item " +
           "WHERE payment_status = :paymentStatus AND payment_id IS NULL ORDER BY return_id DESC",
           nativeQuery = true)
    List<ReturnItem> findByPaymentStatusAndPaymentIdIsNullOptimized(@Param("paymentStatus") String paymentStatus);
    
    List<ReturnItem> findByPaymentStatusAndPaymentIdIsNull(String paymentStatus);
    
    /**
     * 배송비 입금 ID로 조회 (인덱스 활용)
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_PAYMENT_ID) */ * FROM tb_return_item " +
           "WHERE payment_id = :paymentId ORDER BY return_id DESC",
           nativeQuery = true)
    List<ReturnItem> findByPaymentIdOptimized(@Param("paymentId") Long paymentId);
    
    List<ReturnItem> findByPaymentId(Long paymentId);
    
    // 🎯 상단 카드 대시보드 통계 쿼리들 (성능 최적화)
    
    /**
     * 회수완료 건수 (인덱스 활용) - 전체완료 제외
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_COLLECTION) */ COUNT(*) FROM tb_return_item WHERE collection_completed_date IS NOT NULL AND (is_completed = 0 OR is_completed IS NULL)", nativeQuery = true)
    long countByCollectionCompletedDateIsNotNull();
    
    /**
     * 회수미완료 건수 (인덱스 활용) - 전체완료 제외
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_COLLECTION) */ COUNT(*) FROM tb_return_item WHERE collection_completed_date IS NULL AND (is_completed = 0 OR is_completed IS NULL)", nativeQuery = true)
    long countByCollectionCompletedDateIsNull();
    
    /**
     * 물류확인 건수 (인덱스 활용) - 전체완료 제외
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_LOGISTICS) */ COUNT(*) FROM tb_return_item WHERE logistics_confirmed_date IS NOT NULL AND (is_completed = 0 OR is_completed IS NULL)", nativeQuery = true)
    long countByLogisticsConfirmedDateIsNotNull();
    
    /**
     * 물류미확인 건수 (인덱스 활용) - 전체완료 제외
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_LOGISTICS) */ COUNT(*) FROM tb_return_item WHERE logistics_confirmed_date IS NULL AND (is_completed = 0 OR is_completed IS NULL)", nativeQuery = true)
    long countByLogisticsConfirmedDateIsNull();
    
    /**
     * 교환 출고완료 건수 (복합 인덱스 활용) - 전체완료 제외
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_TYPE_SHIP) */ COUNT(*) FROM tb_return_item " +
           "WHERE return_type_code IN ('FULL_EXCHANGE', 'PARTIAL_EXCHANGE', 'EXCHANGE') AND shipping_date IS NOT NULL AND (is_completed = 0 OR is_completed IS NULL)", 
           nativeQuery = true)
    long countExchangeShipped();
    
    /**
     * 교환 출고미완료 건수 (복합 인덱스 활용) - 전체완료 제외
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_TYPE_SHIP) */ COUNT(*) FROM tb_return_item " +
           "WHERE return_type_code IN ('FULL_EXCHANGE', 'PARTIAL_EXCHANGE', 'EXCHANGE') AND shipping_date IS NULL AND (is_completed = 0 OR is_completed IS NULL)", 
           nativeQuery = true)
    long countExchangeNotShipped();
    
    /**
     * 반품 환불완료 건수 (복합 인덱스 활용) - 전체완료 제외
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_TYPE_REFUND) */ COUNT(*) FROM tb_return_item " +
           "WHERE return_type_code IN ('FULL_RETURN', 'PARTIAL_RETURN') AND refund_date IS NOT NULL AND (is_completed = 0 OR is_completed IS NULL)", 
           nativeQuery = true)
    long countReturnRefunded();
    
    /**
     * 반품 환불미완료 건수 (복합 인덱스 활용) - 전체완료 제외
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_TYPE_REFUND) */ COUNT(*) FROM tb_return_item " +
           "WHERE return_type_code IN ('FULL_RETURN', 'PARTIAL_RETURN') AND refund_date IS NULL AND (is_completed = 0 OR is_completed IS NULL)", 
           nativeQuery = true)
    long countReturnNotRefunded();
    
    /**
     * 배송비 입금상태별 건수 (복합 인덱스 활용) - 전체완료 제외
     * 전체반품, 부분반품, 교환 모두 포함
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_TYPE_PAY) */ COUNT(*) FROM tb_return_item " +
           "WHERE return_type_code IN ('FULL_EXCHANGE', 'PARTIAL_EXCHANGE', 'EXCHANGE', 'FULL_RETURN', 'PARTIAL_RETURN') AND payment_status = :paymentStatus AND (is_completed = 0 OR is_completed IS NULL)", 
           nativeQuery = true)
    long countExchangeByPaymentStatus(@Param("paymentStatus") String paymentStatus);
    
    /**
     * 🔥 입금예정 건수 - 두 조건 모두 고려 (payment_status = 'PENDING' OR shipping_fee = '입금예정') - 전체완료 제외
     * 전체반품, 부분반품, 교환 모두 포함
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_TYPE_PAY) */ COUNT(*) FROM tb_return_item " +
           "WHERE return_type_code IN ('FULL_EXCHANGE', 'PARTIAL_EXCHANGE', 'EXCHANGE', 'FULL_RETURN', 'PARTIAL_RETURN') " +
           "AND (payment_status = 'PENDING' OR shipping_fee = '입금예정') AND (is_completed = 0 OR is_completed IS NULL)", 
           nativeQuery = true)
    long countExchangeByPaymentPending();
    
    /**
     * 🔥 입금완료 건수 - 정확한 조건 적용 - 전체완료 제외
     * 전체반품, 부분반품, 교환 모두 포함
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_TYPE_PAY) */ COUNT(*) FROM tb_return_item " +
           "WHERE return_type_code IN ('FULL_EXCHANGE', 'PARTIAL_EXCHANGE', 'EXCHANGE', 'FULL_RETURN', 'PARTIAL_RETURN') " +
           "AND payment_status = 'COMPLETED' AND (is_completed = 0 OR is_completed IS NULL)", 
           nativeQuery = true)
    long countExchangeByPaymentCompleted();
    
    // 🎯 카드 필터링용 페이징 메서드들 (성능 최적화)
    
    /**
     * 회수완료 데이터 페이징 조회 (인덱스 활용) - 전체완료 제외
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_COLLECTION) */ * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT * FROM tb_return_item " +
           "    WHERE collection_completed_date IS NOT NULL AND (is_completed = 0 OR is_completed IS NULL) " +
           "    ORDER BY return_id DESC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findByCollectionCompletedDateIsNotNull(@Param("startRow") int startRow, @Param("endRow") int endRow);
    
    /**
     * 회수미완료 데이터 페이징 조회 (인덱스 활용) - 전체완료 제외
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_COLLECTION) */ * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT * FROM tb_return_item " +
           "    WHERE collection_completed_date IS NULL AND (is_completed = 0 OR is_completed IS NULL) " +
           "    ORDER BY return_id DESC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findByCollectionCompletedDateIsNull(@Param("startRow") int startRow, @Param("endRow") int endRow);
    
    /**
     * 물류확인완료 데이터 페이징 조회 (인덱스 활용) - 전체완료 제외
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_LOGISTICS) */ * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT * FROM tb_return_item " +
           "    WHERE logistics_confirmed_date IS NOT NULL AND (is_completed = 0 OR is_completed IS NULL) " +
           "    ORDER BY return_id DESC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findByLogisticsConfirmedDateIsNotNull(@Param("startRow") int startRow, @Param("endRow") int endRow);
    
    /**
     * 물류확인미완료 데이터 페이징 조회 (인덱스 활용) - 전체완료 제외
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_LOGISTICS) */ * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT * FROM tb_return_item " +
           "    WHERE logistics_confirmed_date IS NULL AND (is_completed = 0 OR is_completed IS NULL) " +
           "    ORDER BY return_id DESC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findByLogisticsConfirmedDateIsNull(@Param("startRow") int startRow, @Param("endRow") int endRow);
    
    /**
     * 교환 출고완료 데이터 페이징 조회 (복합 인덱스 활용) - 전체완료 제외
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_TYPE_SHIP) */ * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT * FROM tb_return_item " +
           "    WHERE return_type_code IN ('FULL_EXCHANGE', 'PARTIAL_EXCHANGE', 'EXCHANGE') " +
           "    AND shipping_date IS NOT NULL AND (is_completed = 0 OR is_completed IS NULL) " +
           "    ORDER BY return_id DESC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findExchangeShipped(@Param("startRow") int startRow, @Param("endRow") int endRow);
    
    /**
     * 교환 출고미완료 데이터 페이징 조회 (복합 인덱스 활용) - 전체완료 제외
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_TYPE_SHIP) */ * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT * FROM tb_return_item " +
           "    WHERE return_type_code IN ('FULL_EXCHANGE', 'PARTIAL_EXCHANGE', 'EXCHANGE') " +
           "    AND shipping_date IS NULL AND (is_completed = 0 OR is_completed IS NULL) " +
           "    ORDER BY return_id DESC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findExchangeNotShipped(@Param("startRow") int startRow, @Param("endRow") int endRow);
    
    /**
     * 반품 환불완료 데이터 페이징 조회 (복합 인덱스 활용) - 전체완료 제외
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_TYPE_REFUND) */ * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT * FROM tb_return_item " +
           "    WHERE return_type_code IN ('FULL_RETURN', 'PARTIAL_RETURN') " +
           "    AND refund_date IS NOT NULL AND (is_completed = 0 OR is_completed IS NULL) " +
           "    ORDER BY return_id DESC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findReturnRefunded(@Param("startRow") int startRow, @Param("endRow") int endRow);
    
    /**
     * 반품 환불미완료 데이터 페이징 조회 (복합 인덱스 활용) - 전체완료 제외
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_TYPE_REFUND) */ * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT * FROM tb_return_item " +
           "    WHERE return_type_code IN ('FULL_RETURN', 'PARTIAL_RETURN') " +
           "    AND refund_date IS NULL AND (is_completed = 0 OR is_completed IS NULL) " +
           "    ORDER BY return_id DESC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findReturnNotRefunded(@Param("startRow") int startRow, @Param("endRow") int endRow);
    

    
    /**
     * 배송비 입금상태별 데이터 페이징 조회 (복합 인덱스 활용) - 전체완료 제외
     * 전체반품, 부분반품, 교환 모두 포함
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_TYPE_PAY) */ * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT * FROM tb_return_item " +
           "    WHERE return_type_code IN ('FULL_EXCHANGE', 'PARTIAL_EXCHANGE', 'EXCHANGE', 'FULL_RETURN', 'PARTIAL_RETURN') " +
           "    AND payment_status = :paymentStatus AND (is_completed = 0 OR is_completed IS NULL) " +
           "    ORDER BY return_id DESC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findExchangeByPaymentStatus(@Param("paymentStatus") String paymentStatus, @Param("startRow") int startRow, @Param("endRow") int endRow);
    
    /**
     * 🔥 입금예정 데이터 페이징 조회 - 두 조건 모두 고려 (payment_status = 'PENDING' OR shipping_fee = '입금예정') - 전체완료 제외
     * 전체반품, 부분반품, 교환 모두 포함
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_TYPE_PAY) */ * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT * FROM tb_return_item " +
           "    WHERE return_type_code IN ('FULL_EXCHANGE', 'PARTIAL_EXCHANGE', 'EXCHANGE', 'FULL_RETURN', 'PARTIAL_RETURN') " +
           "    AND (payment_status = 'PENDING' OR shipping_fee = '입금예정') AND (is_completed = 0 OR is_completed IS NULL) " +
           "    ORDER BY return_id DESC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findExchangeByPaymentPending(@Param("startRow") int startRow, @Param("endRow") int endRow);
    
    /**
     * 🔥 입금완료 데이터 페이징 조회 - 정확한 조건 적용 - 전체완료 제외
     * 전체반품, 부분반품, 교환 모두 포함
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_TYPE_PAY) */ * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT * FROM tb_return_item " +
           "    WHERE return_type_code IN ('FULL_EXCHANGE', 'PARTIAL_EXCHANGE', 'EXCHANGE', 'FULL_RETURN', 'PARTIAL_RETURN') " +
           "    AND payment_status = 'COMPLETED' AND (is_completed = 0 OR is_completed IS NULL) " +
           "    ORDER BY return_id DESC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findExchangeByPaymentCompleted(@Param("startRow") int startRow, @Param("endRow") int endRow);
    
    /**
     * 브랜드별 전체 교환/반품 조회 (매핑 후보 확장용)
     */
    @Query("SELECT r FROM ReturnItem r WHERE " +
           "LOWER(r.siteName) LIKE LOWER(CONCAT('%', :brandKeyword, '%')) " +
           "ORDER BY r.id DESC")
    List<ReturnItem> findBySiteNameContainingBrand(@Param("brandKeyword") String brandKeyword);
    
    /**
     * 브랜드별 + 고객명/연락처 조건 교환/반품 조회 (확장 검색용)
     */
    @Query("SELECT r FROM ReturnItem r WHERE " +
           "LOWER(r.siteName) LIKE LOWER(CONCAT('%', :brandKeyword, '%')) AND " +
           "(LOWER(r.customerName) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR " +
           "r.customerPhone LIKE CONCAT('%', :searchKeyword, '%')) " +
           "ORDER BY r.id DESC")
    List<ReturnItem> findBySiteNameContainingBrandAndCustomerInfo(
            @Param("brandKeyword") String brandKeyword, 
            @Param("searchKeyword") String searchKeyword);
    
    /**
     * 완료 상태별 건수 조회 (통계용)
     */
    @Query(value = "SELECT COUNT(*) FROM tb_return_item WHERE is_completed = 1", nativeQuery = true)
    long countByIsCompletedTrue();
    
    @Query(value = "SELECT COUNT(*) FROM tb_return_item WHERE is_completed = 0 OR is_completed IS NULL", nativeQuery = true)
    long countByIsCompletedFalse();
    
    /**
     * 완료된 데이터 페이징 조회
     */
    @Query(value = "SELECT * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT * FROM tb_return_item " +
           "    WHERE is_completed = 1 " +
           "    ORDER BY return_id DESC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findByCompleted(@Param("startRow") int startRow, @Param("endRow") int endRow);
    
    /**
     * 미완료 데이터 페이징 조회
     */
    @Query(value = "SELECT * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT * FROM tb_return_item " +
           "    WHERE is_completed = 0 OR is_completed IS NULL " +
           "    ORDER BY return_id DESC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findByIncompleted(@Param("startRow") int startRow, @Param("endRow") int endRow);

    @Query(value = "SELECT COUNT(*) FROM tb_return_item " +
           "WHERE (:keyword IS NULL OR " +
           "       order_number LIKE '%' || :keyword || '%' OR " +
           "       customer_name LIKE '%' || :keyword || '%' OR " +
           "       customer_phone LIKE '%' || :keyword || '%' OR " +
           "       site_name LIKE '%' || :keyword || '%' OR " +
           "       order_item_code LIKE '%' || :keyword || '%' OR " +
           "       product_color LIKE '%' || :keyword || '%' OR " +
           "       product_size LIKE '%' || :keyword || '%' OR " +
           "       tracking_number LIKE '%' || :keyword || '%' OR " +
           "       return_reason LIKE '%' || :keyword || '%' OR " +
           "       return_type_code LIKE '%' || :keyword || '%' OR " +
           "       CASE return_type_code " +
           "           WHEN 'FULL_RETURN' THEN '전체반품' " +
           "           WHEN 'PARTIAL_RETURN' THEN '부분반품' " +
           "           WHEN 'FULL_EXCHANGE' THEN '전체교환' " +
           "           WHEN 'PARTIAL_EXCHANGE' THEN '부분교환' " +
           "           WHEN 'EXCHANGE' THEN '일반교환' " +
           "           ELSE return_type_code " +
           "       END LIKE '%' || :keyword || '%' OR " +
           "       CASE " +
           "           WHEN (is_completed = 1 OR is_completed IS NULL) THEN '완료' " +
           "           WHEN is_completed = 0 THEN '미완료' " +
           "           ELSE '미완료' " +
           "       END LIKE '%' || :keyword || '%' OR " +
           "       (:keyword = 'Y' AND is_completed = 1) OR " +
           "       (:keyword = 'N' AND (is_completed = 0 OR is_completed IS NULL)) OR " +
           "       (:keyword = '완료' AND is_completed = 1) OR " +
           "       (:keyword = '전체완료' AND is_completed = 1) OR " +
           "       (:keyword = '미완료' AND (is_completed = 0 OR is_completed IS NULL)) OR " +
           "       (:keyword = '진행중' AND (is_completed = 0 OR is_completed IS NULL)) OR " +
           "       processor LIKE '%' || :keyword || '%' OR " +
           "       remarks LIKE '%' || :keyword || '%') " +
           "AND (:startDate IS NULL OR cs_received_date >= :startDate) " +
           "AND (:endDate IS NULL OR cs_received_date <= :endDate)", 
           nativeQuery = true)
    long countBySearchCriteria(
            @Param("keyword") String keyword,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    // 🎯 처리기간 임박 필터 - 접수일 기준 10일 이상 미완료 데이터 조회
    
    /**
     * 접수일 기준 10일 이상 미완료 건수 조회 (성능 최적화)
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_COMPLETION_DATE) */ COUNT(*) FROM tb_return_item " +
           "WHERE cs_received_date < :tenDaysAgo AND (is_completed = 0 OR is_completed IS NULL)", 
           nativeQuery = true)
    long countOverdueTenDays(@Param("tenDaysAgo") LocalDateTime tenDaysAgo);
    
    /**
     * 접수일 기준 10일 이상 미완료 데이터 페이징 조회 (성능 최적화)
     */
    @Query(value = "SELECT /*+ INDEX(tb_return_item IDX_RETURN_ITEM_COMPLETION_DATE) */ * FROM (" +
           "  SELECT a.*, ROWNUM rnum FROM (" +
           "    SELECT * FROM tb_return_item " +
           "    WHERE cs_received_date < :tenDaysAgo AND (is_completed = 0 OR is_completed IS NULL) " +
           "    ORDER BY cs_received_date ASC" +
           "  ) a WHERE ROWNUM <= :endRow" +
           ") WHERE rnum > :startRow", 
           nativeQuery = true)
    List<ReturnItem> findOverdueTenDays(@Param("tenDaysAgo") LocalDateTime tenDaysAgo, 
                                        @Param("startRow") int startRow, 
                                        @Param("endRow") int endRow);
}