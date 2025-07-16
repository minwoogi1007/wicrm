package com.wio.crm.mapper;

import com.wio.crm.dto.CallLogDto;
import com.wio.crm.dto.LmsLogDto;
import com.wio.crm.dto.LmsTrackingSearchDto;
import com.wio.crm.dto.LmsTrackingStatsDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * LMS 문자 발송 추적 MyBatis 매퍼
 * 
 * @author 개발팀
 * @since 2025.01
 */
@Mapper
public interface LmsTrackingMapper {
    
    // ========== LMS 발송 내역 관련 ==========
    
    /**
     * LMS 발송 내역 목록 조회 (페이징)
     */
    List<LmsLogDto> findLmsList(@Param("search") LmsTrackingSearchDto search);
    
    /**
     * LMS 발송 내역 총 개수 조회
     */
    long countLmsList(@Param("search") LmsTrackingSearchDto search);
    
    /**
     * LMS 발송 내역 상세 조회
     */
    LmsLogDto findLmsById(@Param("id") Long id);
    
    // ========== 통화 내역 관련 ==========
    
    /**
     * 특정 LMS 발송 건의 후속 통화 내역 조회
     */
    List<CallLogDto> findCallHistoryByLms(@Param("lmsId") Long lmsId);
    
    /**
     * 전화번호와 날짜 기준 후속 통화 내역 조회
     */
    List<CallLogDto> findCallHistoryByPhoneAndDate(
            @Param("clid") String clid, 
            @Param("sendDate") String sendDate,
            @Param("custCode") String custCode);
    
    // ========== 통계 관련 ==========
    
    /**
     * 기본 통계 조회
     */
    Map<String, Object> getBasicStats(@Param("search") LmsTrackingSearchDto search);
    
    /**
     * 시간대별 통계 조회
     */
    List<Map<String, Object>> getTimeSlotStats(@Param("search") LmsTrackingSearchDto search);
    
    /**
     * 상담원별 성과 통계 조회
     */
    List<Map<String, Object>> getAgentStats(@Param("search") LmsTrackingSearchDto search);
    
    /**
     * 일별 트렌드 통계 조회 (최근 7일)
     */
    List<Map<String, Object>> getDailyTrendStats(@Param("search") LmsTrackingSearchDto search);
    
    /**
     * 메시지 유형별 통계 조회
     */
    List<Map<String, Object>> getMessageTypeStats(@Param("search") LmsTrackingSearchDto search);
    
    /**
     * 평균 응답시간 조회
     */
    Double getAvgResponseTime(@Param("search") LmsTrackingSearchDto search);
    
    /**
     * 🚀 통합 LMS 통계 조회 (성능 최적화) - 6개 쿼리를 1개로 통합
     */
    Map<String, Object> getUnifiedStats(@Param("search") LmsTrackingSearchDto search);
    
    // ========== 검증 및 디버깅 ==========
    
    /**
     * 테이블 존재 여부 확인
     */
    int checkLmsLogTableExists();
    
    /**
     * 테이블 존재 여부 확인
     */
    int checkCallLogTableExists();
    
    /**
     * 샘플 데이터 개수 조회 (디버깅용)
     */
    Map<String, Object> getSampleDataCount(@Param("custCode") String custCode);
    
    /**
     * 대량 후속 연락 정보 조회 (N+1 문제 해결용)
     */
    List<Map<String, Object>> findCallHistoryBatchByLmsIds(@Param("lmsIds") List<Long> lmsIds, @Param("custCode") String custCode);
} 