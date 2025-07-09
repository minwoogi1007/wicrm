package com.wio.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🚀 교환반품 통계 내부 데이터 구조체 모음 (신규 설계)
 * 
 * 목적: 매퍼와 서비스 간 데이터 전달용 중간 객체들
 * 특징: 간단하고 명확한 데이터 구조, DB 결과와 1:1 매핑
 * 
 * @author 개발팀
 * @since 2025.01
 */
public class ExchangeStatsData {

    /**
     * 📊 핵심 통계 데이터
     * 
     * TB_RETURN_ITEM 테이블에서 집계된 기본 통계 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CoreStats {
        private Long totalCount;           // 총 건수
        private Long completedCount;       // 완료 건수 (IS_COMPLETED = 1)
        private Long incompleteCount;      // 미완료 건수 (IS_COMPLETED = 0 OR NULL)
        private Double completionRate;     // 완료율 (%)
        private Long totalRefundAmount;    // 총 환불금액
        private Long totalShippingFee;     // 총 배송비
    }

    /**
     * 📈 트렌드 분석 데이터 포인트
     * 
     * 일별/주별/월별 추이 분석용 단일 데이터 포인트
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrendPoint {
        private String dateLabel;          // 날짜 라벨 (YYYY-MM-DD, YYYY-WW, YYYY-MM 등)
        private Long totalCount;           // 해당 기간 총 건수
        private Long completedCount;       // 해당 기간 완료 건수
        private Long incompleteCount;      // 해당 기간 미완료 건수
        private Long refundAmount;         // 해당 기간 환불금액
        private Double completionRate;     // 해당 기간 완료율
    }

    /**
     * 🥧 유형별 분포 데이터
     * 
     * RETURN_TYPE_CODE 기준 분류 통계
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TypeDistribution {
        private String returnType;         // 교환반품 유형 코드 (FULL_EXCHANGE, PARTIAL_EXCHANGE 등)
        private Long count;                // 해당 유형 건수
        private Long totalRefundAmount;    // 해당 유형 총 환불금액
        private Double averageRefundAmount; // 해당 유형 평균 환불금액
    }

    /**
     * 🍩 상태별 분포 데이터
     * 
     * IS_COMPLETED 기준 완료/미완료 분류
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusDistribution {
        private Boolean isCompleted;       // 완료 상태 (true=완료, false=미완료)
        private Long count;                // 해당 상태 건수
        private Double percentage;         // 해당 상태 비율
    }

    /**
     * 📋 사유별 분석 데이터
     * 
     * RETURN_REASON 기준 분류 통계
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReasonAnalysis {
        private String returnReason;       // 반품 사유
        private Long count;                // 해당 사유 건수
        private Long totalRefundAmount;    // 해당 사유 총 환불금액
        private Double averageRefundAmount; // 해당 사유 평균 환불금액
    }

    /**
     * 🏢 사이트별 성과 데이터
     * 
     * SITE_NAME 기준 분류 통계
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SitePerformance {
        private String siteName;           // 사이트명
        private Long totalCount;           // 해당 사이트 총 건수
        private Long completedCount;       // 해당 사이트 완료 건수
        private Long incompleteCount;      // 해당 사이트 미완료 건수
        private Double completionRate;     // 해당 사이트 완료율
        private Long totalRefundAmount;    // 해당 사이트 총 환불금액
        private Long totalShippingFee;     // 해당 사이트 총 배송비
    }

    /**
     * 🔍 검색 조건 정보
     * 
     * 실제 적용된 WHERE 절 조건들의 요약
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchConditions {
        private String dateColumn;         // 사용된 날짜 컬럼 (CS_RECEIVED_DATE, ORDER_DATE)
        private String startDate;          // 시작 날짜
        private String endDate;            // 종료 날짜
        private String returnTypeFilter;   // 유형 필터
        private String completionFilter;   // 완료 상태 필터
        private String siteNameFilter;     // 사이트명 필터
        private Integer appliedFilterCount; // 적용된 필터 개수
    }

    /**
     * ⚡ 성능 정보
     * 
     * 쿼리 실행 시간 및 성능 측정 데이터
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PerformanceInfo {
        private Long queryExecutionTimeMs; // 쿼리 실행 시간 (밀리초)
        private Long totalProcessingTimeMs; // 전체 처리 시간 (밀리초)
        private Integer recordsProcessed;   // 처리된 레코드 수
        private Double recordsPerSecond;    // 초당 처리 레코드 수
    }

    /**
     * 🔧 시스템 헬스 정보
     * 
     * 통계 시스템의 상태 확인용 데이터
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SystemHealth {
        private Boolean databaseConnected;  // DB 연결 상태
        private Boolean tableExists;        // TB_RETURN_ITEM 테이블 존재 여부
        private Long recentDataCount;       // 최근 데이터 건수 (7일 이내)
        private String lastUpdateTime;      // 마지막 데이터 업데이트 시간
        private Boolean systemHealthy;      // 전체 시스템 정상 여부
    }
} 