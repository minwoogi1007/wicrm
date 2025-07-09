package com.wio.crm.mapper;

import com.wio.crm.dto.ExchangeStatsData;
import com.wio.crm.dto.ExchangeStatsRequestDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 🚀 교환반품 통계 전용 매퍼 (신규 설계)
 * 
 * 목적: TB_RETURN_ITEM 테이블 기준 통계 데이터 조회
 * 특징: 단순하고 명확한 SQL, 성능 최적화된 쿼리
 * 
 * @author 개발팀
 * @since 2025.01
 */
@Mapper
public interface ExchangeStatsMapper {

    /**
     * 📊 핵심 통계 조회
     * 
     * 총 건수, 완료율, 환불금액, 배송비 등 기본 집계 데이터 조회
     * 
     * @param requestDto 조회 조건
     * @return 핵심 통계 데이터
     */
    ExchangeStatsData.CoreStats getCoreStats(@Param("request") ExchangeStatsRequestDto requestDto);

    /**
     * 📈 트렌드 데이터 조회
     * 
     * 날짜별 추이 분석용 데이터 조회 (일별/주별/월별)
     * 
     * @param requestDto 조회 조건
     * @return 트렌드 데이터 포인트 리스트
     */
    List<ExchangeStatsData.TrendPoint> getTrendData(@Param("request") ExchangeStatsRequestDto requestDto);

    /**
     * 🥧 유형별 분포 조회
     * 
     * RETURN_TYPE_CODE 기준 분류 통계
     * 
     * @param requestDto 조회 조건
     * @return 유형별 분포 데이터 리스트
     */
    List<ExchangeStatsData.TypeDistribution> getTypeDistribution(@Param("request") ExchangeStatsRequestDto requestDto);

    /**
     * 🍩 상태별 분포 조회
     * 
     * IS_COMPLETED 기준 완료/미완료 분류
     * 
     * @param requestDto 조회 조건
     * @return 상태별 분포 데이터 리스트
     */
    List<ExchangeStatsData.StatusDistribution> getStatusDistribution(@Param("request") ExchangeStatsRequestDto requestDto);

    /**
     * 📋 사유별 분석 조회
     * 
     * RETURN_REASON 기준 분류 통계
     * 
     * @param requestDto 조회 조건
     * @return 사유별 분석 데이터 리스트
     */
    List<ExchangeStatsData.ReasonAnalysis> getReasonAnalysis(@Param("request") ExchangeStatsRequestDto requestDto);

    /**
     * 🏢 사이트별 성과 조회
     * 
     * SITE_NAME 기준 분류 통계
     * 
     * @param requestDto 조회 조건
     * @return 사이트별 성과 데이터 리스트
     */
    List<ExchangeStatsData.SitePerformance> getSitePerformance(@Param("request") ExchangeStatsRequestDto requestDto);

    // ==================== 헬스체크 및 디버깅용 메서드들 ====================

    /**
     * 🔗 데이터베이스 연결 확인
     * 
     * @return 연결 상태 (true=연결됨, false=연결 안됨)
     */
    Boolean checkDatabaseConnection();

    /**
     * 📋 테이블 존재 확인
     * 
     * @param tableName 확인할 테이블명
     * @return 존재 여부 (true=존재, false=없음)
     */
    Boolean checkTableExists(@Param("tableName") String tableName);

    /**
     * 📊 최근 데이터 건수 확인
     * 
     * 최근 7일 이내 데이터 건수 조회
     * 
     * @return 최근 데이터 건수
     */
    Long getRecentDataCount();

    /**
     * 🔍 검색 조건 유효성 확인
     * 
     * 주어진 조건으로 조회 가능한 데이터가 있는지 확인
     * 
     * @param requestDto 조회 조건
     * @return 조회 가능한 데이터 건수
     */
    Long validateSearchConditions(@Param("request") ExchangeStatsRequestDto requestDto);

    /**
     * ⚡ 쿼리 성능 테스트
     * 
     * 주어진 조건으로 쿼리 실행 시간 측정
     * 
     * @param requestDto 조회 조건
     * @return 성능 정보
     */
    ExchangeStatsData.PerformanceInfo measureQueryPerformance(@Param("request") ExchangeStatsRequestDto requestDto);
} 