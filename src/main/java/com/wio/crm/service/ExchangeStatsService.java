package com.wio.crm.service;

import com.wio.crm.dto.ExchangeStatsRequestDto;
import com.wio.crm.dto.ExchangeStatsResponseDto;

import java.util.Map;

/**
 * 🚀 교환반품 통계 서비스 인터페이스 (신규 설계)
 * 
 * 목적: 기존의 복잡한 StatisticsService와 ReturnItemService의 통계 관련 기능 통합
 * 특징: 단순하고 명확한 API 제공, 높은 응집도
 * 
 * @author 개발팀
 * @since 2025.01
 */
public interface ExchangeStatsService {

    /**
     * 📊 교환반품 통계 데이터 조회 (통합)
     * 
     * 모든 통계 데이터를 한번에 조회하는 핵심 메서드
     * 
     * 포함 데이터:
     * - 핵심 지표: 총 건수, 완료율, 총 환불금액, 총 배송비
     * - 트렌드 분석: 일별/주별/월별 추이 데이터
     * - 분포 분석: 유형별, 상태별 분포
     * - 상세 분석: 사유별, 사이트별 통계
     * 
     * @param requestDto 조회 조건 (날짜, 유형, 상태, 사이트 등)
     * @return 모든 통계 데이터가 포함된 응답 DTO
     * @throws IllegalArgumentException 잘못된 요청 파라미터
     * @throws RuntimeException 데이터 조회 실패
     */
    ExchangeStatsResponseDto getExchangeStats(ExchangeStatsRequestDto requestDto);

    /**
     * 🐛 교환반품 통계 디버깅 정보 조회
     * 
     * 개발 및 운영 시 문제 진단을 위한 상세 정보 제공
     * 
     * 포함 정보:
     * - 실행된 SQL 쿼리 조건
     * - 각 단계별 조회 결과 수
     * - 처리 시간 정보
     * - 적용된 필터 조건 요약
     * 
     * @param requestDto 조회 조건
     * @return 디버깅 정보 맵
     * @throws RuntimeException 디버깅 정보 조회 실패
     */
    Map<String, Object> getDebugInfo(ExchangeStatsRequestDto requestDto);

    /**
     * 📈 교환반품 통계 헬스체크
     * 
     * 통계 시스템의 상태를 확인하는 메서드
     * 
     * 확인 항목:
     * - 데이터베이스 연결 상태
     * - 필수 테이블 존재 여부
     * - 최근 데이터 업데이트 시간
     * - 통계 계산 성능
     * 
     * @return 헬스체크 결과 정보
     */
    Map<String, Object> getHealthCheck();
} 