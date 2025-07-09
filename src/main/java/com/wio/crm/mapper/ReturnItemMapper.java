package com.wio.crm.mapper;

import com.wio.crm.model.ReturnItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 교환/반품 관련 매퍼
 */
@Mapper
public interface ReturnItemMapper {
    
    /**
     * 교환/반품 목록 조회 (검색 조건 포함)
     */
    List<ReturnItem> selectReturnItemList(@Param("searchParams") Map<String, Object> searchParams);
    
    /**
     * 교환/반품 목록 총 개수
     */
    int selectReturnItemCount(@Param("searchParams") Map<String, Object> searchParams);
    
    /**
     * 교환/반품 상세 조회
     */
    ReturnItem selectReturnItemById(@Param("returnId") Long returnId);
    
    /**
     * 교환/반품 등록
     */
    int insertReturnItem(ReturnItem returnItem);
    
    /**
     * 교환/반품 수정
     */
    int updateReturnItem(ReturnItem returnItem);
    
    /**
     * 교환/반품 삭제
     */
    int deleteReturnItem(@Param("returnId") Long returnId);
    
    /**
     * 교환/반품 상태 업데이트
     */
    int updateReturnItemStatus(@Param("returnId") Long returnId, 
                               @Param("returnStatusCode") String returnStatusCode,
                               @Param("updatedBy") String updatedBy);
    
    /**
     * 일괄 상태 업데이트
     */
    int updateReturnItemStatusBatch(@Param("returnIds") List<Long> returnIds,
                                    @Param("returnStatusCode") String returnStatusCode,
                                    @Param("updatedBy") String updatedBy);
    
    /**
     * 통계 데이터 - 상태별 카운트
     */
    List<Map<String, Object>> selectReturnItemStatusStats();
    
    /**
     * 통계 데이터 - 월별 교환/반품 건수
     */
    List<Map<String, Object>> selectMonthlyReturnItemStats(@Param("year") Integer year);
    
    /**
     * 교환/반품 타입별 통계
     */
    List<Map<String, Object>> selectReturnItemTypeStats(@Param("startDate") String startDate, 
                                                         @Param("endDate") String endDate);
    
    // ==================== 카운트 메서드들 ====================
    
    /**
     * 전체 건수
     */
    Long getTotalCount();
    
    /**
     * 🎯 오늘 등록 건수 (실제 오늘 날짜 기준)
     */
    Long getTodayRegisteredCount();
    
    /**
     * 어제까지 등록 건수
     */
    Long getUntilYesterdayCount();
    
    /**
     * 회수완료 건수
     */
    Long getCollectionCompletedCount();
    
    /**
     * 회수미완료 건수
     */
    Long getCollectionPendingCount();
    
    /**
     * 물류확인완료 건수
     */
    Long getLogisticsConfirmedCount();
    
    /**
     * 물류확인미완료 건수
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
     * 배송비 입금미완료 건수
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
    
    // ==================== 실시간 통계 메서드들 ====================
    
    /**
     * 🎯 사유별 통계
     */
    List<Map<String, Object>> getReasonCounts();
    
    /**
     * 🎯 사이트별 통계
     */
    List<Map<String, Object>> getSiteCounts();
    
    /**
     * 🎯 금액 요약 통계
     */
    Map<String, Object> getAmountSummary();
    
    /**
     * 🎯 브랜드별 통계 (배송비 결제 테이블 조인)
     */
    List<Map<String, Object>> getBrandCounts();
    
    /**
     * 🎯 유형별 상세 통계  
     */
    List<Map<String, Object>> getTypeCounts();
    
    // ==================== 필터링 검색 메서드들 ====================
    
    /**
     * 🎯 필터링 포함 검색 조건 기반 조회 (Oracle ROWNUM 페이징)
     */
    List<ReturnItem> findBySearchCriteria(@Param("searchParams") Map<String, Object> searchParams);
    
    /**
     * 🎯 필터링 포함 검색 조건 기반 총 개수 
     */
    Long countBySearchCriteria(@Param("searchParams") Map<String, Object> searchParams);
    
    // ==================== 성능 최적화된 메서드들 ====================
    
    /**
     * 🚀 성능 최적화된 목록 조회 (기존 인덱스 활용)
     */
    List<ReturnItem> selectReturnItemListOptimized(@Param("searchParams") Map<String, Object> searchParams);
    
    /**
     * 🚀 성능 최적화된 COUNT 쿼리 (인덱스 활용)
     */
    Long selectReturnItemCountOptimized(@Param("searchParams") Map<String, Object> searchParams);
    
    /**
     * 📈 트렌드 차트용 경량 데이터 조회 (성능 최적화)
     */
    List<Map<String, Object>> selectReturnItemListForTrend(@Param("searchParams") Map<String, Object> searchParams);
    
    /**
     * 🚀 통합 대시보드 통계 (한 번의 쿼리로 모든 통계 조회)
     */
    Map<String, Object> getDashboardStatsOptimized(@Param("searchParams") Map<String, Object> searchParams);
    
    /**
     * 🚀 통합 대시보드 통계 - 전체 데이터 기준 (기존 12개 쿼리 → 1개 쿼리)
     */
    Map<String, Object> getDashboardStatsUnified(@Param("searchParams") Map<String, Object> searchParams);
    
    /**
     * 🚀 통합 대시보드 통계 - 검색 조건별 (검색 조건 적용)
     */
    Map<String, Object> getDashboardStatsBySearch(@Param("searchParams") Map<String, Object> searchParams);
    
    /**
     * 🚀 상태별 조회 최적화 (복합 인덱스 활용)
     */
    List<ReturnItem> selectByStatusOptimized(@Param("status") String status,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate,
                                           @Param("startRow") Integer startRow,
                                           @Param("endRow") Integer endRow);
    
    /**
     * 🚀 타입별 조회 최적화 (복합 인덱스 활용)
     */
    List<ReturnItem> selectByTypeOptimized(@Param("type") String type,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate,
                                         @Param("startRow") Integer startRow,
                                         @Param("endRow") Integer endRow);
    
    /**
     * 🚀 처리 지연 건 조회 (14일 이상 미완료)
     */
    List<ReturnItem> selectDelayedItems(@Param("startRow") Integer startRow,
                                      @Param("endRow") Integer endRow);
    
    /**
     * 🚀 고객정보 기반 자동 매핑 조회 (복합 인덱스 활용)
     */
    List<ReturnItem> selectByCustomerForMapping(@Param("customerName") String customerName);
    
    // ==================== 교환반품 통계 전용 메서드들 ====================
    
    /**
     * 📊 조건에 따른 교환반품 데이터 조회 (통계용)
     */
    List<Map<String, Object>> getReturnItemsByCondition(@Param("searchParams") Map<String, Object> searchParams);
    
    // ==================== 검색조건 기반 통계 메서드들 ==================== 
    
    /**
     * 🎯 조건 기반 전체 건수
     */
    Long getTotalCountByCondition(@Param("dateFilterType") String dateFilterType,
                                  @Param("csStartDate") String csStartDate,
                                  @Param("csEndDate") String csEndDate,
                                  @Param("orderStartDate") String orderStartDate,
                                  @Param("orderEndDate") String orderEndDate,
                                  @Param("returnType") String returnType,
                                  @Param("siteName") String siteName,
                                  @Param("completionStatus") String completionStatus,
                                  @Param("keyword") String keyword);
    
    /**
     * 🎯 조건 기반 완료 건수
     */
    Long getCompletedCountByCondition(@Param("dateFilterType") String dateFilterType,
                                      @Param("csStartDate") String csStartDate,
                                      @Param("csEndDate") String csEndDate,
                                      @Param("orderStartDate") String orderStartDate,
                                      @Param("orderEndDate") String orderEndDate,
                                      @Param("returnType") String returnType,
                                      @Param("siteName") String siteName,
                                      @Param("completionStatus") String completionStatus,
                                      @Param("keyword") String keyword);
    
    /**
     * 🎯 조건 기반 미완료 건수
     */
    Long getIncompletedCountByCondition(@Param("dateFilterType") String dateFilterType,
                                        @Param("csStartDate") String csStartDate,
                                        @Param("csEndDate") String csEndDate,
                                        @Param("orderStartDate") String orderStartDate,
                                        @Param("orderEndDate") String orderEndDate,
                                        @Param("returnType") String returnType,
                                        @Param("siteName") String siteName,
                                        @Param("completionStatus") String completionStatus,
                                        @Param("keyword") String keyword);
    
    /**
     * 🎯 조건 기반 유형별 통계
     */
    List<Map<String, Object>> getTypeCountsByCondition(@Param("dateFilterType") String dateFilterType,
                                                        @Param("csStartDate") String csStartDate,
                                                        @Param("csEndDate") String csEndDate,
                                                        @Param("orderStartDate") String orderStartDate,
                                                        @Param("orderEndDate") String orderEndDate,
                                                        @Param("returnType") String returnType,
                                                        @Param("siteName") String siteName,
                                                        @Param("completionStatus") String completionStatus,
                                                        @Param("keyword") String keyword);
    
    /**
     * 🎯 조건 기반 사유별 통계
     */
    List<Map<String, Object>> getReasonCountsByCondition(@Param("dateFilterType") String dateFilterType,
                                                          @Param("csStartDate") String csStartDate,
                                                          @Param("csEndDate") String csEndDate,
                                                          @Param("orderStartDate") String orderStartDate,
                                                          @Param("orderEndDate") String orderEndDate,
                                                          @Param("returnType") String returnType,
                                                          @Param("siteName") String siteName,
                                                          @Param("completionStatus") String completionStatus,
                                                          @Param("keyword") String keyword);
    
    /**
     * 🎯 조건 기반 사이트별 통계
     */
    List<Map<String, Object>> getSiteCountsByCondition(@Param("dateFilterType") String dateFilterType,
                                                        @Param("csStartDate") String csStartDate,
                                                        @Param("csEndDate") String csEndDate,
                                                        @Param("orderStartDate") String orderStartDate,
                                                        @Param("orderEndDate") String orderEndDate,
                                                        @Param("returnType") String returnType,
                                                        @Param("siteName") String siteName,
                                                        @Param("completionStatus") String completionStatus,
                                                        @Param("keyword") String keyword);
    
    /**
     * 🎯 조건 기반 금액 요약
     */
    Map<String, Object> getAmountSummaryByCondition(@Param("dateFilterType") String dateFilterType,
                                                     @Param("csStartDate") String csStartDate,
                                                     @Param("csEndDate") String csEndDate,
                                                     @Param("orderStartDate") String orderStartDate,
                                                     @Param("orderEndDate") String orderEndDate,
                                                     @Param("returnType") String returnType,
                                                     @Param("siteName") String siteName,
                                                     @Param("completionStatus") String completionStatus,
                                                     @Param("keyword") String keyword);
    
    /**
     * 🎯 조건 기반 트렌드 데이터
     */
    List<Map<String, Object>> getTrendDataByCondition(@Param("dateFilterType") String dateFilterType,
                                                       @Param("csStartDate") String csStartDate,
                                                       @Param("csEndDate") String csEndDate,
                                                       @Param("orderStartDate") String orderStartDate,
                                                       @Param("orderEndDate") String orderEndDate,
                                                       @Param("returnType") String returnType,
                                                       @Param("siteName") String siteName,
                                                       @Param("completionStatus") String completionStatus,
                                                       @Param("keyword") String keyword);
    
    // ==================== 통계 API용 검색 조건 기반 카운트 메서드들 ====================
    
    /**
     * 회수완료 건수 (검색 조건 적용)
     */
    Long getCollectionCompletedCountBySearch(@Param("startDate") String startDate,
                                           @Param("endDate") String endDate,
                                           @Param("returnTypeCode") String returnTypeCode,
                                           @Param("brandFilter") String brandFilter);
    
    /**
     * 물류확인 건수 (검색 조건 적용)
     */
    Long getLogisticsConfirmedCountBySearch(@Param("startDate") String startDate,
                                          @Param("endDate") String endDate,
                                          @Param("returnTypeCode") String returnTypeCode,
                                          @Param("brandFilter") String brandFilter);
    
    /**
     * 교환 출고완료 건수 (검색 조건 적용)
     */
    Long getExchangeShippedCountBySearch(@Param("startDate") String startDate,
                                       @Param("endDate") String endDate,
                                       @Param("returnTypeCode") String returnTypeCode,
                                       @Param("brandFilter") String brandFilter);
    
    /**
     * 반품 환불완료 건수 (검색 조건 적용)
     */
    Long getReturnRefundedCountBySearch(@Param("startDate") String startDate,
                                      @Param("endDate") String endDate,
                                      @Param("returnTypeCode") String returnTypeCode,
                                      @Param("brandFilter") String brandFilter);
    
    /**
     * 배송비 입금완료 건수 (검색 조건 적용)
     */
    Long getPaymentCompletedCountBySearch(@Param("startDate") String startDate,
                                        @Param("endDate") String endDate,
                                        @Param("returnTypeCode") String returnTypeCode,
                                        @Param("brandFilter") String brandFilter);
    
    /**
     * 전체완료 건수 (검색 조건 적용) - IS_COMPLETED = 1
     */
    Long getCompletedCountBySearch(@Param("startDate") String startDate,
                                 @Param("endDate") String endDate,
                                 @Param("returnTypeCode") String returnTypeCode,
                                 @Param("brandFilter") String brandFilter);
    
    /**
     * 전체미완료 건수 (검색 조건 적용) - IS_COMPLETED = 0 OR NULL
     */
    Long getIncompletedCountBySearch(@Param("startDate") String startDate,
                                   @Param("endDate") String endDate,
                                   @Param("returnTypeCode") String returnTypeCode,
                                   @Param("brandFilter") String brandFilter);
    
    /**
     * 전체 건수 (검색 조건 적용)
     */
    Long getTotalCountBySearch(@Param("startDate") String startDate,
                             @Param("endDate") String endDate,
                             @Param("returnTypeCode") String returnTypeCode,
                             @Param("brandFilter") String brandFilter);
    
    /**
     * 교환 건수 (검색 조건 적용)
     */
    Long getExchangeCountBySearch(@Param("searchParams") Map<String, Object> searchParams);
    
    /**
     * 반품 건수 (검색 조건 적용)
     */
    Long getReturnCountBySearch(@Param("searchParams") Map<String, Object> searchParams);
    
    /**
     * 배송비 필요 건수 (검색 조건 적용)
     */
    Long getPaymentRequiredCountBySearch(@Param("searchParams") Map<String, Object> searchParams);
    
    /**
     * 유형별 건수 (검색 조건 적용)
     */
    Long getCountByTypeAndSearch(@Param("returnType") String returnType, @Param("searchParams") Map<String, Object> searchParams);
    
    /**
     * 검색 조건에 맞는 전체 데이터 (페이징 없이)
     */
    List<ReturnItem> findBySearch(@Param("searchParams") Map<String, Object> searchParams);
    
    /**
     * 페이징 없는 전체 데이터
     */
    List<ReturnItem> findAllItems();
    
    /**
     * 💰 검색 조건 기반 금액 통계 (DB 직접 집계)
     */
    Map<String, Object> getAmountStatsBySearch(@Param("searchParams") Map<String, Object> searchParams);
} 