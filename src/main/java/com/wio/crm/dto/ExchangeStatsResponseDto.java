package com.wio.crm.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ExchangeStatsResponseDto {
    // 핵심 지표 요약 - 새로운 구조로 교체
    private ExchangeStatsData.CoreStats summary;
    
    // 트렌드 분석 데이터
    private List<ExchangeStatsData.TrendPoint> trendData;
    
    // 유형별 분포
    private List<ExchangeStatsData.TypeDistribution> typeDistribution;
    
    // 상태별 분포
    private List<ExchangeStatsData.StatusDistribution> statusDistribution;
    
    // 사유별 분석
    private List<ExchangeStatsData.ReasonAnalysis> reasonAnalysis;
    
    // 사이트별 성과
    private List<ExchangeStatsData.SitePerformance> sitePerformance;
    
    // 호환성을 위한 기존 필드들 (Map 형태로 유지)
    private Map<String, Long> typeCounts;
    private Map<String, Long> statusCounts;
    private Map<String, Long> reasonCounts;
    private Map<String, Long> siteCounts;
    private Map<String, Object> amountSummary;
    
    // 추가 메타 정보
    private String searchPeriod; // 검색 기간
    private String lastUpdated; // 마지막 업데이트 시간
    private Long totalRecords; // 전체 레코드 수
    
    // 기본 생성자
    public ExchangeStatsResponseDto() {
    }
    
    // 간단한 생성자
    public ExchangeStatsResponseDto(ExchangeStatsData.CoreStats summary) {
        this.summary = summary;
    }
} 