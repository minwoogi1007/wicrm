package com.wio.crm.service;

import com.wio.crm.dto.ReturnItemDTO;
import com.wio.crm.dto.ReturnItemSearchDTO;
import com.wio.crm.dto.ReturnItemBulkDateUpdateDTO;
import com.wio.crm.dto.ExchangeStatsRequestDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * 교환/반품 서비스 인터페이스
 */
public interface ReturnItemService {

    /**
     * 전체 목록 조회 (페이징)
     */
    Page<ReturnItemDTO> findAll(int page, int size, String sortBy, String sortDir);

    /**
     * 검색 조건으로 조회
     */
    Page<ReturnItemDTO> search(ReturnItemSearchDTO searchDTO);
    
    /**
     * MyBatis 기반 유연한 검색 조건 조회 (필터 조건 포함)
     */
    Page<ReturnItemDTO> findBySearchCriteria(Map<String, Object> searchParams);
    
    /**
     * 🚨 접수일 기준 10일 이상 미완료 데이터 조회
     */
    Page<ReturnItemDTO> findOverdueTenDays(ReturnItemSearchDTO searchDTO);
    
    /**
     * 🎯 다중 필터 처리 (Sample 통합)
     */
    Page<ReturnItemDTO> findByMultipleFilters(List<String> filters, ReturnItemSearchDTO searchDTO);
    
    /**
     * 🎯 다중 필터 + 검색 조건 함께 처리 (Sample 통합)
     */
    Page<ReturnItemDTO> findByMultipleFiltersWithSearch(List<String> filters, ReturnItemSearchDTO searchDTO);
    
    /**
     * 🎯 다중 필터 조회 (페이징 없음, 엑셀 다운로드용)
     */
    List<ReturnItemDTO> findByMultipleFiltersUnlimited(List<String> filters, ReturnItemSearchDTO searchDTO);

    /**
     * ID로 조회
     */
    ReturnItemDTO findById(Long id);

    /**
     * 저장
     */
    ReturnItemDTO save(ReturnItemDTO returnItemDTO);
    
    /**
     * 수정 (컨트롤러 호환용)
     */
    ReturnItemDTO updateReturnItem(ReturnItemDTO returnItemDTO);

    /**
     * 새 교환/반품 건 생성 (엑셀 업로드용)
     */
    ReturnItemDTO createReturnItem(ReturnItemDTO returnItemDTO);

    /**
     * 삭제
     */
    void delete(Long id);

    /**
     * 상태별 통계
     */
    Map<String, Long> getStatusCounts();

    /**
     * 사이트별 통계
     */
    Map<String, Long> getSiteCounts();

    /**
     * 유형별 통계
     */
    Map<String, Long> getTypeCounts();

    /**
     * 사유별 통계
     */
    Map<String, Long> getReasonCounts();

    /**
     * 금액 요약
     */
    Map<String, Object> getAmountSummary();

    /**
     * 브랜드별 통계
     */
    Map<String, Long> getBrandCounts();

    /**
     * 금일 등록 건수
     */
    Long getTodayCount();
    
    /**
     * 전체 등록 건수
     */
    Long getTotalCount();
    
    /**
     * 어제까지 등록 건수
     */
    Long getUntilYesterdayCount();
    
    /**
     * 완료율 계산 (전체 중 완료된 비율)
     */
    Double getCompletionRate();

    /**
     * 회수완료 건수
     */
    Long getCollectionCompletedCount();

    /**
     * 회수미완료 건수
     */
    Long getCollectionPendingCount();

    /**
     * 물류확인 완료 건수
     */
    Long getLogisticsConfirmedCount();

    /**
     * 물류확인 미완료 건수
     */
    Long getLogisticsPendingCount();

    /**
     * 교환 출고완료 건수
     */
    Long getExchangeShippedCount();

    /**
     * 교환 출고미완료 건수
     */
    Long getExchangeNotShippedCount();

    /**
     * 반품 환불완료 건수
     */
    Long getReturnRefundedCount();

    /**
     * 반품 환불미완료 건수
     */
    Long getReturnNotRefundedCount();

    /**
     * 배송비 입금완료 건수
     */
    Long getPaymentCompletedCount();

    /**
     * 배송비 입금대기 건수
     */
    Long getPaymentPendingCount();

    /**
     * 완료 건수
     */
    Long getCompletedCount();

    /**
     * 미완료 건수
     */
    Long getIncompletedCount();
    
    /**
     * 10일 경과 건수 조회 (Sample에서 통합)
     */
    Long getOverdueTenDaysCount();



    /**
     * 일괄 날짜 업데이트
     */
    int bulkUpdateDates(List<ReturnItemBulkDateUpdateDTO> updates);

    /**
     * 완료 상태 업데이트
     */
    boolean updateCompletionStatus(Long id, Boolean isCompleted);

    // 🎯 카드 필터링 메서드들 - sample 폴더에서 참고하여 추가
    
    /**
     * 회수완료 필터링
     */
    Page<ReturnItemDTO> findByCollectionCompleted(ReturnItemSearchDTO searchDTO);
    
    /**
     * 회수미완료 필터링
     */
    Page<ReturnItemDTO> findByCollectionPending(ReturnItemSearchDTO searchDTO);
    
    /**
     * 물류확인완료 필터링
     */
    Page<ReturnItemDTO> findByLogisticsConfirmed(ReturnItemSearchDTO searchDTO);
    
    /**
     * 물류확인미완료 필터링
     */
    Page<ReturnItemDTO> findByLogisticsPending(ReturnItemSearchDTO searchDTO);
    
    /**
     * 출고완료 필터링
     */
    Page<ReturnItemDTO> findByShippingCompleted(ReturnItemSearchDTO searchDTO);
    
    /**
     * 출고대기 필터링
     */
    Page<ReturnItemDTO> findByShippingPending(ReturnItemSearchDTO searchDTO);
    
    /**
     * 환불완료 필터링
     */
    Page<ReturnItemDTO> findByRefundCompleted(ReturnItemSearchDTO searchDTO);
    
    /**
     * 환불대기 필터링
     */
    Page<ReturnItemDTO> findByRefundPending(ReturnItemSearchDTO searchDTO);
    
    /**
     * 입금완료 필터링
     */
    Page<ReturnItemDTO> findByPaymentCompleted(ReturnItemSearchDTO searchDTO);
    
    /**
     * 입금대기 필터링
     */
    Page<ReturnItemDTO> findByPaymentPending(ReturnItemSearchDTO searchDTO);
    
    /**
     * 전체완료 필터링
     */
    Page<ReturnItemDTO> findByCompleted(ReturnItemSearchDTO searchDTO);
    
    /**
     * 미완료 필터링
     */
    Page<ReturnItemDTO> findByIncompleted(ReturnItemSearchDTO searchDTO);

    // 기존 메서드들 (호환성 유지)
    Map<String, Object> getDashboardStats();
    Map<String, Object> getDashboardStats(Map<String, Object> searchParams);
    Map<String, Object> validateAndCleanSearchParams(Map<String, Object> params);
    Map<String, Object> getReturnItemList(Map<String, Object> searchParams);
    Object getReturnItemById(Long returnId);
    boolean updateReturnItemStatus(Long returnId, String returnStatusCode, String updatedBy);
    boolean updateReturnItemStatusBatch(List<Long> returnIds, String returnStatusCode, String updatedBy);
    List<Map<String, Object>> getReturnItemStatusStats();
    List<Map<String, Object>> getReturnItemTypeStats(String startDate, String endDate);
    
    /**
     * 🚀 실시간 8개 통계 조회 (신규)
     * 1. 전체 건수 (오늘까지/오늘/어제까지)
     * 2. 교환반품 현황 (완료/미완료)
     * 3. 유형별 현황 (EXCHANGE, RETURN, FULL_RETURN, PARTIAL_RETURN)
     * 4. 배송비 합계 (숫자 데이터만)
     * 5. 환불금 합계
     * 6. 반품교환 사유 통계
     * 7. 사이트별 통계
     * 8. 브랜드별 통계
     */
    Map<String, Object> getRealtimeStats();

    // 🎯 검색 조건 기반 통계 메서드들 (ReturnItemSearchDTO 사용)
    
    /**
     * 검색 조건 기반 상태별 통계
     */
    Map<String, Long> getStatusCountsBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 사이트별 통계
     */
    Map<String, Long> getSiteCountsBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 유형별 통계
     */
    Map<String, Long> getTypeCountsBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 사유별 통계
     */
    Map<String, Long> getReasonCountsBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 금액 요약
     */
    Map<String, Object> getAmountSummaryBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 브랜드별 통계
     */
    Map<String, Long> getBrandCountsBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 금일 등록 건수
     */
    Long getTodayCountBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 회수완료 건수
     */
    Long getCollectionCompletedCountBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 회수미완료 건수
     */
    Long getCollectionPendingCountBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 물류확인 완료 건수
     */
    Long getLogisticsConfirmedCountBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 물류확인 미완료 건수
     */
    Long getLogisticsPendingCountBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 교환 출고완료 건수
     */
    Long getExchangeShippedCountBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 교환 출고미완료 건수
     */
    Long getExchangeNotShippedCountBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 반품 환불완료 건수
     */
    Long getReturnRefundedCountBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 반품 환불미완료 건수
     */
    Long getReturnNotRefundedCountBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 배송비 입금완료 건수
     */
    Long getPaymentCompletedCountBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 배송비 입금대기 건수
     */
    Long getPaymentPendingCountBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 완료 건수
     */
    Long getCompletedCountBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 미완료 건수
     */
    Long getIncompletedCountBySearch(ReturnItemSearchDTO searchDTO);

    // 📊 통계 페이지 전용 추가 메서드들
    
    /**
     * 검색 조건 기반 교환 건수
     */
    Long getExchangeCountBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 반품 건수
     */
    Long getReturnCountBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건 기반 배송비 필요 건수
     */
    Long getPaymentRequiredCountBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 유형별 건수 (검색 조건 포함)
     */
    Long getCountByTypeAndSearch(String returnType, ReturnItemSearchDTO searchDTO);
    
    /**
     * 트렌드 데이터 (차트용)
     */
    Map<String, Object> getTrendDataBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 전체 건수 (검색 조건 포함)
     */
    Long getTotalCountBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 검색 조건에 맞는 전체 데이터 리스트
     */
    List<ReturnItemDTO> findBySearch(ReturnItemSearchDTO searchDTO);
    
    /**
     * 전체 데이터 리스트 (페이징 없음)
     */
    List<ReturnItemDTO> findAll();

    // 🚀 성능 최적화: 통합 통계 메소드들 추가
    
    /**
     * 통합 대시보드 통계 조회 (전체 데이터 기준)
     * 기존 12개 개별 쿼리를 1개 통합 쿼리로 최적화
     */
    Map<String, Object> getDashboardStatsUnified();
    
    /**
     * 통합 대시보드 통계 조회 (검색 조건 기준)
     * 검색 조건이 적용된 데이터에 대한 통합 통계
     */
    Map<String, Object> getDashboardStatsUnifiedBySearch(ReturnItemSearchDTO searchDTO);

    // 🆕 이미지 관리 메소드들
    
    /**
     * 이미지 URL 업데이트
     */
    void updateDefectPhotoUrl(Long itemId, String imageUrl);
    
    /**
     * 불량상세 메모 업데이트
     */
    void updateDefectDetail(Long itemId, String defectDetail);
} 