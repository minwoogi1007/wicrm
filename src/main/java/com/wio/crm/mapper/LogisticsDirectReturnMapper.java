package com.wio.crm.mapper;

import com.wio.crm.dto.LogisticsDirectReturnDTO;
import com.wio.crm.dto.LogisticsDirectReturnSearchDTO;
import com.wio.crm.model.LogisticsDirectReturn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 물류센터 직접 입고 관리 MyBatis 매퍼
 */
@Mapper
public interface LogisticsDirectReturnMapper {
    
    // ========== 기본 CRUD ==========
    
    /**
     * 전체 목록 조회 (페이징)
     */
    List<LogisticsDirectReturnDTO> findAll(@Param("search") LogisticsDirectReturnSearchDTO search);
    
    /**
     * 전체 개수 조회
     */
    long countAll(@Param("search") LogisticsDirectReturnSearchDTO search);
    
    /**
     * ID로 조회
     */
    LogisticsDirectReturnDTO findById(@Param("id") Long id);
    
    /**
     * 등록
     */
    void insert(LogisticsDirectReturn logisticsDirectReturn);
    
    /**
     * 수정
     */
    void update(LogisticsDirectReturn logisticsDirectReturn);
    
    /**
     * 삭제
     */
    void delete(@Param("id") Long id);
    
    // ========== 검색 관련 ==========
    
    /**
     * 검색 조건으로 목록 조회
     */
    List<LogisticsDirectReturnDTO> findBySearchCriteria(@Param("search") LogisticsDirectReturnSearchDTO search);
    
    /**
     * 검색 조건으로 개수 조회
     */
    long countBySearchCriteria(@Param("search") LogisticsDirectReturnSearchDTO search);
    
    /**
     * 키워드로 검색
     */
    List<LogisticsDirectReturnDTO> findByKeyword(@Param("keyword") String keyword, 
                                                 @Param("offset") int offset, 
                                                 @Param("size") int size);
    
    // ========== 매핑 관련 ==========
    
    /**
     * 운송장번호로 기존 교환반품 데이터 찾기
     */
    List<Map<String, Object>> findReturnItemsByTrackingNumber(@Param("trackingNumber") String trackingNumber);
    
    /**
     * 매핑 처리
     */
    void updateMapping(@Param("id") Long id, 
                      @Param("matchedReturnId") Long matchedReturnId, 
                      @Param("mappingStatus") String mappingStatus,
                      @Param("mappingBy") String mappingBy);
    
    /**
     * 매핑 해제
     */
    void clearMapping(@Param("id") Long id, @Param("updatedBy") String updatedBy);
    
    /**
     * 매핑 통계 조회
     */
    Map<String, Object> getMappingStatistics();
    
    // ========== 일괄 처리 ==========
    
    /**
     * 일괄 삭제
     */
    void deleteBatch(@Param("ids") List<Long> ids);
    
    /**
     * 일괄 상태 변경
     */
    void updateStatusBatch(@Param("ids") List<Long> ids, 
                          @Param("processingStatus") String processingStatus,
                          @Param("updatedBy") String updatedBy);
    
    /**
     * 일괄 매핑 해제
     */
    void clearMappingBatch(@Param("ids") List<Long> ids, @Param("updatedBy") String updatedBy);
    
    // ========== 통계 및 집계 ==========
    
    /**
     * 일별 입고 통계
     */
    List<Map<String, Object>> getDailyStatistics(@Param("startDate") String startDate, 
                                                 @Param("endDate") String endDate);
    
    /**
     * 사이트별 통계
     */
    List<Map<String, Object>> getSiteStatistics(@Param("startDate") String startDate, 
                                               @Param("endDate") String endDate);
    
    /**
     * 처리 상태별 통계
     */
    Map<String, Object> getStatusStatistics();
    
    /**
     * 오늘 입고 통계
     */
    Map<String, Object> getTodayStatistics();
    
    // ========== 데이터 검증 ==========
    
    /**
     * 운송장번호 중복 체크
     */
    boolean existsByTrackingNumber(@Param("trackingNumber") String trackingNumber, 
                                  @Param("excludeId") Long excludeId);
    
    /**
     * 같은 고객의 동일 제품 중복 체크
     */
    boolean existsDuplicateProduct(@Param("customerName") String customerName,
                                  @Param("productCode") String productCode,
                                  @Param("excludeId") Long excludeId);
    
    // ========== 엑셀 다운로드용 ==========
    
    /**
     * 엑셀 다운로드용 전체 데이터 조회 (페이징 없음)
     */
    List<LogisticsDirectReturnDTO> findAllForExcel(@Param("search") LogisticsDirectReturnSearchDTO search);
} 