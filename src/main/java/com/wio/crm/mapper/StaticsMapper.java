package com.wio.crm.mapper;

import com.wio.crm.model.DashboardData;
import com.wio.crm.model.Statics;
import com.wio.crm.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface StaticsMapper {

    List<Statics> getStatisticsCons(@Param("start") String start, @Param("end") String end, @Param("custCode") String custCode);
    List<Statics> getStatisticsConsG(@Param("start") String start, @Param("end") String end,@Param("custCode") String custCode);

    List<Statics> getConsultationResult(@Param("start") String start, @Param("end") String end, @Param("custCode") String custCode);
    List<Statics> getConsultationResultG(@Param("start") String start, @Param("end") String end, @Param("custCode") String custCode);

    List<Statics> getConsultationTime(@Param("start") String start, @Param("end") String end, @Param("custCode") String custCode);
    List<Statics> getConsultationTimeG(@Param("start") String start, @Param("end") String end, @Param("custCode") String custCode);

    public List<Map<String, Object>> getStatCons(Map<String, Object> map);
    public List<Map<String, Object>> getStatResult(Map<String, Object> map);
    public List<Map<String, Object>> getStatTime(Map<String, Object> map);

    DailySummaryStatsDto getDailySummaryStats(@Param("date") String date, @Param("custCode") String custCode);
    DailyCallTimeStatsDto getDailyCallTimeStats(@Param("date") String date, @Param("custCode") String custCode);
    List<DailyCounselingTypeStatsDto> getDailyCounselingTypeStats(@Param("date") String date, @Param("custCode") String custCode);
    Integer getTotalCallsForPreviousYear(@Param("previousYearDate") String previousYearDate, @Param("custCode") String custCode);
    Integer getCompletedCallsForPreviousYear(@Param("previousYearDate") String previousYearDate, @Param("custCode") String custCode);

    YearlyComparisonDataDto getYearlyTotalCallsComparison(@Param("currentPeriodStartDate") String currentPeriodStartDate, @Param("previousPeriodStartDate") String previousPeriodStartDate, @Param("custCode") String custCode);
    YearlyComparisonDataDto getYearlyCompletedCounselingComparison(@Param("currentPeriodStartDate") String currentPeriodStartDate, @Param("previousPeriodStartDate") String previousPeriodStartDate, @Param("custCode") String custCode);

    // 주간 통계
    List<WeeklySummaryStatsDto> getWeeklySummaryStats(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("custCode") String custCode);
    List<WeeklyCounselingTypeStatsDto> getWeeklyCounselingTypeStats(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("custCode") String custCode);
    // WeeklyCallTimeStatsDto getWeeklyCallTimeStats(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("custCode") String custCode); // 통화시간 현황 미구현

    // 전년 동기 주간 데이터 조회 (비교용)
    Integer getWeeklyTotalCallsForComparison(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("custCode") String custCode);
    Integer getWeeklyCompletedCallsForComparison(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("custCode") String custCode);

    // 월간 통계
    MonthlySummaryStatsDto getMonthlySummaryStats(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("custCode") String custCode);
    MonthlyCallTimeStatsDto getMonthlyCallTimeStats(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("custCode") String custCode);
    List<MonthlyCounselingTypeStatsDto> getMonthlyCounselingTypeStats(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("custCode") String custCode);

    // 월간 일별 통계 조회 (차트용)
    List<DailySummaryStatsDto> getDailyStatsForMonth(@Param("startDate") String startDate, 
                                               @Param("endDate") String endDate, 
                                               @Param("custCode") String custCode);
    
    // 월간 주차별 통계 조회
    List<WeeklySummaryStatsDto> getWeeklyStatsForMonth(@Param("startDate") String startDate, 
                                                 @Param("endDate") String endDate, 
                                                 @Param("custCode") String custCode);
    
    // 월간 전월 대비 비교 데이터 조회
    YearlyComparisonDataDto getMonthlyComparisonData(@Param("currentStartDate") String currentStartDate, 
                                               @Param("previousStartDate") String previousStartDate, 
                                               @Param("custCode") String custCode);
                                               
    // 시간대별 통화량 데이터 조회
    List<Map<String, Object>> getHourlyCallsData(@Param("date") String date,
                                           @Param("yesterdayDate") String yesterdayDate,
                                           @Param("custCode") String custCode);
                                           
    // 월간 비교 통계 조회 메서드 추가
    Integer getMonthlyTotalCallsForComparison(@Param("startDate") String startDate, 
                                        @Param("endDate") String endDate, 
                                        @Param("custCode") String custCode);
    
    Integer getMonthlyCompletedCallsForComparison(@Param("startDate") String startDate, 
                                            @Param("endDate") String endDate, 
                                            @Param("custCode") String custCode);
    
    Double getMonthlyCompletionRate(@Param("startDate") String startDate, 
                              @Param("endDate") String endDate, 
                              @Param("custCode") String custCode);
}
