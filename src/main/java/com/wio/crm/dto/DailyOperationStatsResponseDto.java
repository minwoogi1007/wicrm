package com.wio.crm.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DailyOperationStatsResponseDto {
    private DailySummaryStatsDto summaryStats;
    private DailyCallTimeStatsDto callTimeStats;
    private List<DailyCounselingTypeStatsDto> counselingTypeStats;
    private HourlyCallsDataDto hourlyCallsData;

    // 기본 생성자
    public DailyOperationStatsResponseDto() {
    }

    // 모든 필드를 포함하는 생성자 (필요에 따라 추가)
    public DailyOperationStatsResponseDto(DailySummaryStatsDto summaryStats, DailyCallTimeStatsDto callTimeStats, List<DailyCounselingTypeStatsDto> counselingTypeStats, HourlyCallsDataDto hourlyCallsData) {
        this.summaryStats = summaryStats;
        this.callTimeStats = callTimeStats;
        this.counselingTypeStats = counselingTypeStats;
        this.hourlyCallsData = hourlyCallsData;
    }
} 