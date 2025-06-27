package com.wio.repairsystem.service;

import com.wio.repairsystem.dto.ReturnItemDTO;
import com.wio.repairsystem.dto.ReturnItemSearchDTO;
import com.wio.repairsystem.dto.ReturnItemBulkDateUpdateDTO;
import com.wio.repairsystem.model.ReturnStatus;
import com.wio.repairsystem.model.ReturnType;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 교환/반품 서비스 인터페이스 (성능 최적화 포함)
 * 
 * 사용 화면:
 * - templates/exchange/list.html : 목록 조회, 검색, 통계
 * - templates/exchange/form.html : 등록, 수정
 * - templates/exchange/view.html : 상세 조회
 * 
 * 주요 기능: CRUD, 검색, 상태 관리, 통계
 * 성능 최적화: 인덱스 활용 쿼리, 최적화된 검색
 */

public interface ReturnItemService {
    
    List<ReturnItemDTO> getAllReturnItems(ReturnItemSearchDTO searchDTO);
    
    ReturnItemDTO createReturnItem(ReturnItemDTO returnItemDTO);
    
    ReturnItemDTO updateReturnItem(ReturnItemDTO returnItemDTO);
    
    ReturnItemDTO getReturnItemById(Long id);
    
    void deleteReturnItem(Long id);
    
    ReturnItemDTO save(ReturnItemDTO returnItemDTO);
    
    ReturnItemDTO update(Long id, ReturnItemDTO returnItemDTO);
    
    ReturnItemDTO findById(Long id);
    
    void delete(Long id);
    
    Page<ReturnItemDTO> findAll(int page, int size, String sortBy, String sortDir);
    
    Page<ReturnItemDTO> search(ReturnItemSearchDTO searchDTO);
    
    Page<ReturnItemDTO> findByStatus(ReturnStatus status, int page, int size);
    
    Page<ReturnItemDTO> findByType(ReturnType type, int page, int size);
    
    Page<ReturnItemDTO> findByOrderNumber(String orderNumber, int page, int size);
    
    Page<ReturnItemDTO> findByCustomerName(String customerName, int page, int size);
    
    Page<ReturnItemDTO> findByDateRange(LocalDate startDate, LocalDate endDate, int page, int size);
    
    List<ReturnItemDTO> findUnprocessed();
    
    ReturnItemDTO updateStatus(Long id, ReturnStatus status);
    
    Map<ReturnStatus, Long> getStatusCounts();
    
    // 🚀 성능 최적화 메서드들 (만 개 데이터 대응)
    
    /**
     * 성능 최적화된 통합 검색 (인덱스 활용)
     */
    Page<ReturnItemDTO> searchOptimized(String keyword, LocalDate startDate, LocalDate endDate, int page, int size);
    
    /**
     * 성능 최적화된 상태별 조회 (인덱스 활용)
     */
    Page<ReturnItemDTO> findByStatusOptimized(String status, int page, int size);
    
    /**
     * 성능 최적화된 타입별 조회 (인덱스 활용)
     */
    Page<ReturnItemDTO> findByTypeOptimized(String type, int page, int size);
    
    /**
     * 성능 최적화된 배송비 상태별 조회 (인덱스 활용)
     */
    List<ReturnItemDTO> findByPaymentStatusOptimized(String paymentStatus);
    
    /**
     * 성능 최적화된 고객+배송비 상태 조회 (복합 인덱스 활용)
     */
    List<ReturnItemDTO> findByCustomerAndPaymentStatusOptimized(String customerName, String customerPhone, String paymentStatus);
    
    /**
     * 성능 최적화된 미완료 항목 조회 (복합 인덱스 활용)
     */
    List<ReturnItemDTO> findUnprocessedOptimized();
    
    // 추가 통계 메서드들
    Map<String, Long> getSiteCounts();
    
    Map<String, Long> getTypeCounts();
    
    Map<String, Long> getReasonCounts();
    
    Map<String, Object> getAmountSummary();
    
    Map<String, Long> getBrandCounts();
    
    Long getTodayCount();
    
    // 🎯 상단 카드 대시보드 통계 메서드들
    Long getCollectionCompletedCount();
    Long getCollectionPendingCount();
    Long getLogisticsConfirmedCount();
    Long getLogisticsPendingCount();
    Long getExchangeShippedCount();
    Long getExchangeNotShippedCount();
    Long getReturnRefundedCount();
    Long getReturnNotRefundedCount();
    Long getPaymentCompletedCount();
    Long getPaymentPendingCount();
    Long getCompletedCount();
    Long getIncompletedCount();
    
    // 🎯 카드 필터링 메서드들 - 실제 조건에 맞는 데이터 조회
    Page<ReturnItemDTO> findByCollectionCompleted(ReturnItemSearchDTO searchDTO);
    Page<ReturnItemDTO> findByCollectionPending(ReturnItemSearchDTO searchDTO);
    Page<ReturnItemDTO> findByLogisticsConfirmed(ReturnItemSearchDTO searchDTO);
    Page<ReturnItemDTO> findByLogisticsPending(ReturnItemSearchDTO searchDTO);
    Page<ReturnItemDTO> findByShippingCompleted(ReturnItemSearchDTO searchDTO);
    Page<ReturnItemDTO> findByShippingPending(ReturnItemSearchDTO searchDTO);
    Page<ReturnItemDTO> findByRefundCompleted(ReturnItemSearchDTO searchDTO);
    Page<ReturnItemDTO> findByRefundPending(ReturnItemSearchDTO searchDTO);
    Page<ReturnItemDTO> findByPaymentCompleted(ReturnItemSearchDTO searchDTO);
    Page<ReturnItemDTO> findByPaymentPending(ReturnItemSearchDTO searchDTO);
    Page<ReturnItemDTO> findByCompleted(ReturnItemSearchDTO searchDTO);
    Page<ReturnItemDTO> findByIncompleted(ReturnItemSearchDTO searchDTO);
    
    // 🎯 대표님 요청: 리스트에서 직접 날짜 일괄 수정 기능
    /**
     * 일괄 날짜 업데이트 (회수완료, 물류확인, 출고일자, 환불일자)
     * @param updates 수정할 항목들의 리스트
     * @return 업데이트된 건수
     */
    int bulkUpdateDates(List<ReturnItemBulkDateUpdateDTO> updates);
    
    // 🎯 대표님 요청: 완료 상태 업데이트 기능
    /**
     * 완료 상태 업데이트 (IS_COMPLETED 컬럼)
     * @param id 항목 ID
     * @param isCompleted 완료 여부 (true: 완료, false: 미완료)
     * @return 업데이트 성공 여부
     */
    boolean updateCompletionStatus(Long id, Boolean isCompleted);
} 