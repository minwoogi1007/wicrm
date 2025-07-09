package com.wio.crm.dto;

import java.util.List;

public class WeeklyOperationStatsResponseDto {

    private List<WeeklySummaryStatsDto> summaryStatsList;
    private List<WeeklyCounselingTypeStatsDto> counselingTypeStats;
    private YearlyComparisonDataDto yearlyComparison;

    // Getters and Setters
    public List<WeeklySummaryStatsDto> getSummaryStatsList() {
        return summaryStatsList;
    }

    public void setSummaryStatsList(List<WeeklySummaryStatsDto> summaryStatsList) {
        this.summaryStatsList = summaryStatsList;
    }

    public List<WeeklyCounselingTypeStatsDto> getCounselingTypeStats() {
        return counselingTypeStats;
    }

    public void setCounselingTypeStats(List<WeeklyCounselingTypeStatsDto> counselingTypeStats) {
        this.counselingTypeStats = counselingTypeStats;
    }

    public YearlyComparisonDataDto getYearlyComparison() {
        return yearlyComparison;
    }

    public void setYearlyComparison(YearlyComparisonDataDto yearlyComparison) {
        this.yearlyComparison = yearlyComparison;
    }
} 