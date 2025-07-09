package com.wio.crm.dto;

import lombok.Data;
import java.util.List;

/**
 * 월간 운영현황 응답 DTO
 */
@Data
public class MonthlyOperationStatsResponseDto {
    /** 월간 요약 통계 */
    private MonthlySummaryStatsDto summaryStats;
    
    /** 월간 일별 통계 목록 (추이 차트용) */
    private List<DailySummaryStatsDto> dailyStatsList;
    
    /** 월간 주차별 통계 목록 */
    private List<WeeklySummaryStatsDto> weeklyStatsList;
    
    /** 월간 상담유형별 통계 */
    private List<MonthlyCounselingTypeStatsDto> counselingTypeStats;
    
    /** 월간 통화시간 통계 */
    private MonthlyCallTimeStatsDto callTimeStats;
    
    /** 전월 대비 비교 데이터 */
    private YearlyComparisonDataDto yearlyComparisonData;

    // 기본 생성자
    public MonthlyOperationStatsResponseDto() {
    }

    // 모든 필드를 포함하는 생성자
    public MonthlyOperationStatsResponseDto(MonthlySummaryStatsDto summaryStats, List<DailySummaryStatsDto> dailyStatsList, List<WeeklySummaryStatsDto> weeklyStatsList, List<MonthlyCounselingTypeStatsDto> counselingTypeStats, MonthlyCallTimeStatsDto callTimeStats, YearlyComparisonDataDto yearlyComparisonData) {
        this.summaryStats = summaryStats;
        this.dailyStatsList = dailyStatsList;
        this.weeklyStatsList = weeklyStatsList;
        this.counselingTypeStats = counselingTypeStats;
        this.callTimeStats = callTimeStats;
        this.yearlyComparisonData = yearlyComparisonData;
    }
} 