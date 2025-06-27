package com.wio.crm.service;

import com.wio.crm.config.CustomUserDetails;
import com.wio.crm.dto.*;
import com.wio.crm.mapper.StaticsMapper;
import com.wio.crm.model.DateRange;
import com.wio.crm.model.Statics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);
    private static final DateTimeFormatter YYYYMMDD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter YYYY_MM_DD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private StaticsMapper staticsMapper;

    private String getCurrentCustCode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getCustCode();
            } else if (principal instanceof String && principal.equals("anonymousUser")) {
                logger.error("Cannot get custCode: User is anonymous.");
                throw new IllegalStateException("User is canonymous. Cannot determine custCode.");
            } else {
                logger.error("Cannot get custCode: Principal is not an instance of CustomUserDetails. Actual type: {}", 
                             principal != null ? principal.getClass().getName() : "null");
                throw new IllegalStateException("Cannot determine custCode from principal of type: " + 
                                                (principal != null ? principal.getClass().getName() : "null"));
            }
        }
        logger.error("Cannot get custCode: User is not authenticated.");
        throw new IllegalStateException("User is not authenticated.");
    }

    public DailyOperationStatsResponseDto getDailyOperationStatistics(String date) {
        String custCode = getCurrentCustCode();
        logger.info("Fetching daily operation statistics for date: {} and custCode: {}", date, custCode);

        // 날짜 유효성 검증
        if (date == null || date.isEmpty()) {
            throw new IllegalArgumentException("날짜가 비어있습니다");
        }

        // 이미 검증된 YYYYMMDD 형식임을 가정
        DateTimeFormatter formatter;
        LocalDate currentLd;
        
        try {
            // 날짜 형식이 "yyyy-MM-dd"인 경우 해당 포맷터 사용
            formatter = date.contains("-") ? YYYY_MM_DD_FORMATTER : YYYYMMDD_FORMATTER;
            currentLd = LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            logger.error("날짜 파싱 오류 ({}): {}", date, e.getMessage());
            throw e; // 컨트롤러에서 처리
        }
        
        // DB 쿼리용 날짜 형식으로 변환 (필요한 경우)
        String dbFormatDate = date.contains("-") ? currentLd.format(YYYYMMDD_FORMATTER) : date;
        
        // 어제 날짜 계산 (평일 기준: 일요일이면 금요일, 토요일이면 금요일)
        LocalDate yesterdayLd = getYesterdayBusinessDate(currentLd);
        String yesterdayDate = yesterdayLd.format(YYYYMMDD_FORMATTER);
        
        logger.info("Yesterday business date for comparison: {}", yesterdayDate);

        DailyOperationStatsResponseDto responseDto = new DailyOperationStatsResponseDto();

        try {
            DailySummaryStatsDto summaryStats = staticsMapper.getDailySummaryStats(dbFormatDate, custCode);
            DailyCallTimeStatsDto callTimeStats = staticsMapper.getDailyCallTimeStats(dbFormatDate, custCode);
            List<DailyCounselingTypeStatsDto> counselingTypeStatsList = staticsMapper.getDailyCounselingTypeStats(dbFormatDate, custCode);
            
            // 시간대별 통화량 데이터 조회
            HourlyCallsDataDto hourlyCallsData = getHourlyCallsData(dbFormatDate, yesterdayDate, custCode);

            calculateSummaryRates(summaryStats, null, null); // 일일은 summaryStats만 사용
            calculateCounselingTypePercentage(counselingTypeStatsList, summaryStats, null, null, null, null);

            responseDto.setSummaryStats(summaryStats);
            responseDto.setCallTimeStats(callTimeStats);
            responseDto.setCounselingTypeStats(counselingTypeStatsList);
            responseDto.setHourlyCallsData(hourlyCallsData);

        } catch (Exception e) {
            logger.error("Error fetching daily operation statistics: {}", e.getMessage(), e);
            throw new RuntimeException("일일 운영 현황 데이터 조회 중 오류 발생: " + e.getMessage(), e);
        }
        return responseDto;
    }

    public WeeklyOperationStatsResponseDto getWeeklyOperationStatistics(String startDate, String endDate) {
        String custCode = getCurrentCustCode();
        logger.info("Fetching weekly operation statistics for period: {} - {} and custCode: {}", startDate, endDate, custCode);

        // 날짜 형식이 "yyyy-MM-dd"인 경우 해당 포맷터 사용
        DateTimeFormatter formatter = startDate.contains("-") ? YYYY_MM_DD_FORMATTER : YYYYMMDD_FORMATTER;
        LocalDate currentPeriodStartLd = LocalDate.parse(startDate, formatter);
        LocalDate currentPeriodEndLd = LocalDate.parse(endDate, formatter);
        
        // 이전 주 날짜 계산 (7일 전)
        LocalDate previousPeriodStartLd = currentPeriodStartLd.minusDays(7);
        LocalDate previousPeriodEndLd = currentPeriodEndLd.minusDays(7);
        
        // DB 쿼리용 날짜 형식으로 변환 (필요한 경우)
        String dbFormatStartDate = startDate.contains("-") ? 
            currentPeriodStartLd.format(YYYYMMDD_FORMATTER) : startDate;
        String dbFormatEndDate = endDate.contains("-") ? 
            currentPeriodEndLd.format(YYYYMMDD_FORMATTER) : endDate;
        
        // 이전 주 날짜 DB 형식으로 변환
        String previousPeriodStartDate = previousPeriodStartLd.format(YYYYMMDD_FORMATTER);
        String previousPeriodEndDate = previousPeriodEndLd.format(YYYYMMDD_FORMATTER);

        WeeklyOperationStatsResponseDto responseDto = new WeeklyOperationStatsResponseDto();
        try {
            List<WeeklySummaryStatsDto> dailySummaryStatsList = staticsMapper.getWeeklySummaryStats(dbFormatStartDate, dbFormatEndDate, custCode);
            // WeeklyCallTimeStatsDto callTimeStats = staticsMapper.getWeeklyCallTimeStats(dbFormatStartDate, dbFormatEndDate, custCode); // 통화시간 현황 미구현으로 주석 처리
            List<WeeklyCounselingTypeStatsDto> counselingTypeStatsList = staticsMapper.getWeeklyCounselingTypeStats(dbFormatStartDate, dbFormatEndDate, custCode);
            YearlyComparisonDataDto yearlyComparison = getWeeklyYearlyComparisonData(dbFormatStartDate, dbFormatEndDate, previousPeriodStartDate, previousPeriodEndDate, custCode);

            // 각 일자별 요약 통계에 대해 비율 계산
            for (WeeklySummaryStatsDto dailyStat : dailySummaryStatsList) {
                calculateSummaryRates(null, dailyStat, null); // 각 일자별 summaryStats 처리
            }
            
            // 상담 유형 퍼센티지 계산 (주간 전체에 대한 합계 사용)
            if (!dailySummaryStatsList.isEmpty()) {
                // 주간 전체 합계 계산 (필요한 경우)
                WeeklySummaryStatsDto weeklyTotal = calculateWeeklyTotal(dailySummaryStatsList);
                calculateCounselingTypePercentage(null, null, weeklyTotal, counselingTypeStatsList, null, null);
            }

            responseDto.setSummaryStatsList(dailySummaryStatsList); // 메서드명 변경 필요
            // responseDto.setCallTimeStats(callTimeStats); // 통화시간 현황 미구현으로 주석 처리
            responseDto.setCounselingTypeStats(counselingTypeStatsList);
            responseDto.setYearlyComparison(yearlyComparison); // 필드명 수정: yearlyComparisonData -> yearlyComparison

        } catch (Exception e) {
            logger.error("Error fetching weekly operation statistics: {}", e.getMessage(), e);
            throw new RuntimeException("주간 운영 현황 데이터 조회 중 오류 발생: " + e.getMessage(), e);
        }
        return responseDto;
    }

    /**
     * 월간 운영 현황 통계 조회
     */
    public MonthlyOperationStatsResponseDto getMonthlyOperationStatistics(String startDate, String endDate) {
        MonthlyOperationStatsResponseDto responseDto = new MonthlyOperationStatsResponseDto();
        String custCode = getCurrentCustCode();
        
        try {
            logger.info("월간 운영 통계 조회 - 시작: {}, 종료: {}, 고객코드: {}", startDate, endDate, custCode);
            
            // DB 조회 형식으로 변환 (필요시)
            String dbFormatStartDate = startDate;
            String dbFormatEndDate = endDate;
            
            // 전월 시작일 계산 (전월 동일 기간)
            LocalDate start = LocalDate.parse(startDate, YYYYMMDD_FORMATTER);
            LocalDate previousMonth = start.minusMonths(1);
            String previousPeriodStartDate = previousMonth.format(YYYYMMDD_FORMATTER);
            
            // 1. 월간 요약 통계 조회
            MonthlySummaryStatsDto summaryStats = staticsMapper.getMonthlySummaryStats(dbFormatStartDate, dbFormatEndDate, custCode);
            
            // 2. 일별 통계 데이터 조회 (월간 통계 추이 차트용)
            List<DailySummaryStatsDto> dailyStatsList = staticsMapper.getDailyStatsForMonth(dbFormatStartDate, dbFormatEndDate, custCode);
            
            // 3. 주차별 통계 데이터 조회
            List<WeeklySummaryStatsDto> weeklyStatsList = staticsMapper.getWeeklyStatsForMonth(dbFormatStartDate, dbFormatEndDate, custCode);
            
            // 디버깅: 주차별 통계 데이터 확인
            if (weeklyStatsList != null) {
                logger.info("주차별 통계 데이터 조회 성공, 개수: {}", weeklyStatsList.size());
                for (int i = 0; i < weeklyStatsList.size(); i++) {
                    WeeklySummaryStatsDto weekStat = weeklyStatsList.get(i);
                    logger.info("주차 {}: 일자={}, 총콜={}, 응대호={}, 포기호={}, 중복제거호={}, 부재호={}, 완료호={}, 인바운드={}, 아웃바운드={}",
                            i+1, weekStat.getStatDate(), weekStat.getTotalCalls(), 
                            weekStat.getAnsweredCalls(), weekStat.getAbandonedCalls(),
                            weekStat.getUniqueInboundCalls(), weekStat.getBusyCalls(),
                            weekStat.getCompletedCounselingCalls(), weekStat.getInboundCalls(),
                            weekStat.getOutboundCalls());
                }
            } else {
                logger.warn("주차별 통계 데이터 조회 실패 (null 반환)");
            }
            
            // 4. 상담유형별 통계 데이터 조회
            List<MonthlyCounselingTypeStatsDto> counselingTypeStatsList = staticsMapper.getMonthlyCounselingTypeStats(dbFormatStartDate, dbFormatEndDate, custCode);
            if (counselingTypeStatsList != null && !counselingTypeStatsList.isEmpty()) {
                long totalCount = counselingTypeStatsList.stream().mapToLong(MonthlyCounselingTypeStatsDto::getCount).sum();
                for (MonthlyCounselingTypeStatsDto dto : counselingTypeStatsList) {
                    // 비율 계산
                    double percentage = totalCount > 0 ? ((double) dto.getCount() / totalCount) * 100 : 0;
                    dto.setPercentage(percentage);
                    
                    // 완료율 계산 (예: 상담유형별 완료율)
                    dto.setCompletionRate(85.0); // 임시 값, 필요시 실제 데이터 계산 로직 구현
                }
            }
            
            // 5. 통화시간 통계 데이터 조회
            MonthlyCallTimeStatsDto callTimeStats = staticsMapper.getMonthlyCallTimeStats(dbFormatStartDate, dbFormatEndDate, custCode);
            
            // 6. 전월 대비 비교 데이터 조회 및 계산
            YearlyComparisonDataDto yearlyComparison = calculateMonthlyComparison(dbFormatStartDate, dbFormatEndDate, previousPeriodStartDate, custCode);
            
            // 응답 DTO에 데이터 설정
            responseDto.setSummaryStats(summaryStats);
            responseDto.setDailyStatsList(dailyStatsList);
            responseDto.setWeeklyStatsList(weeklyStatsList);
            responseDto.setCounselingTypeStats(counselingTypeStatsList);
            responseDto.setCallTimeStats(callTimeStats);
            responseDto.setYearlyComparisonData(yearlyComparison);
            
        } catch (Exception e) {
            logger.error("Error fetching monthly operation statistics: {}", e.getMessage(), e);
            throw new RuntimeException("월간 운영 현황 데이터 조회 중 오류 발생: " + e.getMessage(), e);
        }
        return responseDto;
    }

    // 공통 로직: 연간 비교 데이터 조회 (시작일 기준)
    private YearlyComparisonDataDto getYearlyComparisonData(String currentPeriodStartDate, String previousPeriodStartDate, String custCode) {
        YearlyComparisonDataDto yearlyComparison = new YearlyComparisonDataDto();
        YearlyComparisonDataDto yearlyTotalCalls = staticsMapper.getYearlyTotalCallsComparison(currentPeriodStartDate, previousPeriodStartDate, custCode);
        YearlyComparisonDataDto yearlyCompletedCounseling = staticsMapper.getYearlyCompletedCounselingComparison(currentPeriodStartDate, previousPeriodStartDate, custCode);

        if (yearlyTotalCalls != null) {
            yearlyComparison.setTotalCallsCurrentYear(yearlyTotalCalls.getTotalCallsCurrentYear());
            yearlyComparison.setTotalCallsPreviousYear(yearlyTotalCalls.getTotalCallsPreviousYear());
        }
        if (yearlyCompletedCounseling != null) {
            yearlyComparison.setCmplCslCurrYr(yearlyCompletedCounseling.getCmplCslCurrYr());
            yearlyComparison.setCmplCslPrevYr(yearlyCompletedCounseling.getCmplCslPrevYr());
        }
        return yearlyComparison;
    }

    // 공통 로직: 응대율, 완료율 계산 (일일, 주간, 월간 DTO 중 하나를 받음)
    private void calculateSummaryRates(DailySummaryStatsDto daily, WeeklySummaryStatsDto weekly, MonthlySummaryStatsDto monthly) {
        long totalCalls = 0, answeredCalls = 0, abandonedCalls = 0, uniqueInboundCalls = 0, completedCounselingCalls = 0;
        long inboundCalls = 0, outboundCalls = 0;
        Object targetDto = null;

        if (daily != null) {
            targetDto = daily; totalCalls = daily.getTotalCalls(); answeredCalls = daily.getAnsweredCalls(); abandonedCalls = daily.getAbandonedCalls();
            uniqueInboundCalls = daily.getUniqueInboundCalls(); completedCounselingCalls = daily.getCompletedCounselingCalls();
            inboundCalls = daily.getInboundCalls(); outboundCalls = daily.getOutboundCalls();
        } else if (weekly != null) {
            targetDto = weekly; totalCalls = weekly.getTotalCalls(); answeredCalls = weekly.getAnsweredCalls(); abandonedCalls = weekly.getAbandonedCalls();
            uniqueInboundCalls = weekly.getUniqueInboundCalls(); completedCounselingCalls = weekly.getCompletedCounselingCalls();
            inboundCalls = weekly.getInboundCalls(); outboundCalls = weekly.getOutboundCalls();
        } else if (monthly != null) {
            targetDto = monthly; totalCalls = monthly.getTotalCalls(); answeredCalls = monthly.getAnsweredCalls(); abandonedCalls = monthly.getAbandonedCalls();
            uniqueInboundCalls = monthly.getUniqueInboundCalls(); completedCounselingCalls = monthly.getCompletedCounselingCalls();
            inboundCalls = monthly.getInboundCalls(); outboundCalls = monthly.getOutboundCalls();
        }

        if (targetDto == null) return;

        long totalCallsForRate = totalCalls - abandonedCalls;
        Double answerRate = (totalCallsForRate > 0) ? (double) answeredCalls * 100 / totalCallsForRate : 0.0;
        Double completionRate = (uniqueInboundCalls > 0) ? (double) completedCounselingCalls * 100 / uniqueInboundCalls : 0.0;
        
        // 인바운드/아웃바운드 비율 계산
        Double inboundRate = (totalCalls > 0) ? (double) inboundCalls * 100 / totalCalls : 0.0;
        Double outboundRate = (totalCalls > 0) ? (double) outboundCalls * 100 / totalCalls : 0.0;

        if (daily != null) { 
            daily.setAnswerRate(answerRate); 
            daily.setCompletionRate(completionRate);
            daily.setInboundRate(inboundRate);
            daily.setOutboundRate(outboundRate);
        }
        else if (weekly != null) { 
            weekly.setAnswerRate(answerRate); 
            weekly.setCompletionRate(completionRate); 
            weekly.setInboundRate(inboundRate);
            weekly.setOutboundRate(outboundRate);
        }
        else if (monthly != null) { 
            monthly.setAnswerRate(answerRate); 
            monthly.setCompletionRate(completionRate);
            monthly.setInboundRate(inboundRate);
            monthly.setOutboundRate(outboundRate);
        }
    }

    // 공통 로직: 상담 유형별 비율 계산
    private void calculateCounselingTypePercentage(List<DailyCounselingTypeStatsDto> dailyList, DailySummaryStatsDto dailySummary,
                                                 WeeklySummaryStatsDto weeklySummary, List<WeeklyCounselingTypeStatsDto> weeklyList,
                                                 MonthlySummaryStatsDto monthlySummary, List<MonthlyCounselingTypeStatsDto> monthlyList) {
        long totalCompletedCounselingForTypeRate = 0;
        
        if (dailyList != null && !dailyList.isEmpty() && dailySummary != null) {
            totalCompletedCounselingForTypeRate = dailySummary.getCompletedCounselingCalls();
            for (DailyCounselingTypeStatsDto typeStat : dailyList) {
                typeStat.setPercentage((totalCompletedCounselingForTypeRate > 0) ? (double) typeStat.getCount() * 100 / totalCompletedCounselingForTypeRate : 0.0);
            }
        } else if (weeklyList != null && !weeklyList.isEmpty() && weeklySummary != null) {
            totalCompletedCounselingForTypeRate = weeklySummary.getCompletedCounselingCalls();
             for (WeeklyCounselingTypeStatsDto typeStat : weeklyList) {
                typeStat.setPercentage((totalCompletedCounselingForTypeRate > 0) ? (double) typeStat.getCount() * 100 / totalCompletedCounselingForTypeRate : 0.0);
            }
        } else if (monthlyList != null && !monthlyList.isEmpty() && monthlySummary != null) { // 월간 처리 추가
            totalCompletedCounselingForTypeRate = monthlySummary.getCompletedCounselingCalls();
            for (MonthlyCounselingTypeStatsDto typeStat : monthlyList) {
                typeStat.setPercentage((totalCompletedCounselingForTypeRate > 0) ? (double) typeStat.getCount() * 100 / totalCompletedCounselingForTypeRate : 0.0);
            }
        }
    }

    // 공통 로직: 전년 동기간 대비 증감률 계산 (주간용)
    private YearlyComparisonDataDto getWeeklyYearlyComparisonData(String currentStartDate, String currentEndDate, String previousStartDate, String previousEndDate, String custCode) {
        YearlyComparisonDataDto comparisonData = new YearlyComparisonDataDto();

        try {
            // 현재 주 총 콜 수
            Integer currentTotalCalls = staticsMapper.getWeeklyTotalCallsForComparison(currentStartDate, currentEndDate, custCode);
            // 이전 주 총 콜 수
            Integer previousTotalCalls = staticsMapper.getWeeklyTotalCallsForComparison(previousStartDate, previousEndDate, custCode);
            // 현재 주 완료 콜 수
            Integer currentCompletedCalls = staticsMapper.getWeeklyCompletedCallsForComparison(currentStartDate, currentEndDate, custCode);
            // 이전 주 완료 콜 수
            Integer previousCompletedCalls = staticsMapper.getWeeklyCompletedCallsForComparison(previousStartDate, previousEndDate, custCode);

            // 값 저장
            comparisonData.setTotalCallsCurrentYear(currentTotalCalls != null ? currentTotalCalls : 0);
            comparisonData.setTotalCallsPreviousYear(previousTotalCalls != null ? previousTotalCalls : 0);
            comparisonData.setCmplCslCurrYr(currentCompletedCalls != null ? currentCompletedCalls : 0);
            comparisonData.setCmplCslPrevYr(previousCompletedCalls != null ? previousCompletedCalls : 0);
            
            // 증감률 계산
            comparisonData.setTotalCallsComparisonRate(calculateRate(currentTotalCalls, previousTotalCalls));
            comparisonData.setCompletedCounselingCallsComparisonRate(calculateRate(currentCompletedCalls, previousCompletedCalls));
            
            logger.info("Weekly comparison data: current total calls={}, previous total calls={}, current completed calls={}, previous completed calls={}",
                     currentTotalCalls, previousTotalCalls, currentCompletedCalls, previousCompletedCalls);
        } catch (Exception e) {
            logger.error("Error calculating weekly comparison data: {}", e.getMessage(), e);
        }
        
        return comparisonData;
    }

    public List<Statics> getStatisticsCons( String start_date,  String end_date) {
        String custCode = getCurrentCustCode();

        try {
            return staticsMapper.getStatisticsCons(start_date, end_date, custCode);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving statistics", e);
        }
    }

    public Map<String, Object> getStatisticsConsG(String start_date,  String end_date) {
        String custCode = getCurrentCustCode();
        Map<String, Object> data = new HashMap<>();
        data.put("statCons", staticsMapper.getStatisticsConsG(start_date,end_date,custCode));
        return data;
    }

    public List<Statics> getConsultationResult( String start_date,  String end_date) {
        String custCode = getCurrentCustCode();

        try {
            return staticsMapper.getConsultationResult(start_date, end_date, custCode);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving statistics", e);
        }
    }

    public Map<String, Object> getConsultationResultG(String start_date,  String end_date) {
        String custCode = getCurrentCustCode();
        Map<String, Object> data = new HashMap<>();
        data.put("statCons", staticsMapper.getConsultationResultG(start_date,end_date,custCode));
        return data;
    }

    public List<Statics> getConsultationTime( String start_date,  String end_date) {
        String custCode = getCurrentCustCode();

        try {
            return staticsMapper.getConsultationTime(start_date, end_date, custCode);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving statistics", e);
        }
    }

    public Map<String, Object> getConsultationTimeG(String start_date,  String end_date) {
        String custCode = getCurrentCustCode();
        Map<String, Object> data = new HashMap<>();
        data.put("statCons", staticsMapper.getConsultationTimeG(start_date,end_date,custCode));
        return data;
    }

    private double calculateRate(Integer current, Integer previous) {
        if (previous == null || previous == 0) {
            return 0.0; // Or handle as an error, or return a specific value like Double.NaN
        }
        if (current == null) {
            current = 0;
        }
        // Calculate the rate of change
        return ((double) (current - previous) / previous) * 100.0;
    }
    
    // Double 타입을 위한 calculateRate 메서드 오버로딩
    private double calculateRate(Double current, Double previous) {
        if (previous == null || previous == 0) {
            return 0.0;
        }
        if (current == null) {
            current = 0.0;
        }
        // Calculate the rate of change
        return ((current - previous) / previous) * 100.0;
    }
    
    // long 타입을 위한 calculateRate 메서드 오버로딩
    private double calculateRate(long current, long previous) {
        if (previous == 0) {
            return 0.0;
        }
        // Calculate the rate of change
        return ((double) (current - previous) / previous) * 100.0;
    }

    // 주간 전체 합계 계산을 위한 새로운 헬퍼 메서드
    private WeeklySummaryStatsDto calculateWeeklyTotal(List<WeeklySummaryStatsDto> dailyStats) {
        WeeklySummaryStatsDto total = new WeeklySummaryStatsDto();
        total.setStatDate("합계");
        
        for (WeeklySummaryStatsDto stat : dailyStats) {
            total.setTotalCalls(total.getTotalCalls() + stat.getTotalCalls());
            total.setAnsweredCalls(total.getAnsweredCalls() + stat.getAnsweredCalls());
            total.setAbandonedCalls(total.getAbandonedCalls() + stat.getAbandonedCalls());
            total.setUniqueInboundCalls(total.getUniqueInboundCalls() + stat.getUniqueInboundCalls());
            total.setBusyCalls(total.getBusyCalls() + stat.getBusyCalls());
            total.setCompletedCounselingCalls(total.getCompletedCounselingCalls() + stat.getCompletedCounselingCalls());
            total.setInboundCalls(total.getInboundCalls() + stat.getInboundCalls());
            total.setOutboundCalls(total.getOutboundCalls() + stat.getOutboundCalls());
        }
        
        // 비율 계산
        calculateSummaryRates(null, total, null);
        
        return total;
    }

    // 어제 날짜 계산 (평일 기준: 일요일이면 금요일, 토요일이면 금요일)
    private LocalDate getYesterdayBusinessDate(LocalDate today) {
        LocalDate yesterday = today.minusDays(1);
        
        // 어제가 일요일(SUNDAY)인 경우 -> 금요일로 설정
        if (yesterday.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            yesterday = yesterday.minusDays(2);
        } 
        // 어제가 토요일(SATURDAY)인 경우 -> 금요일로 설정
        else if (yesterday.getDayOfWeek() == java.time.DayOfWeek.SATURDAY) {
            yesterday = yesterday.minusDays(1);
        }
        
        return yesterday;
    }
    
    // 시간대별 통화량 데이터 가공
    private HourlyCallsDataDto getHourlyCallsData(String date, String yesterdayDate, String custCode) {
        List<Map<String, Object>> hourlyData = staticsMapper.getHourlyCallsData(date, yesterdayDate, custCode);
        
        // 시간대 라벨 (09시 ~ 19시)
        List<String> hours = new java.util.ArrayList<>();
        for (int i = 9; i <= 19; i++) {
            hours.add(String.format("%02d시", i));
        }
        
        // 오늘과 어제의 시간별 통화량을 담을 배열 (9시~19시만 포함)
        List<Integer> todayCalls = new java.util.ArrayList<>(java.util.Collections.nCopies(11, 0)); // 9시부터 19시까지 11개 시간대
        List<Integer> yesterdayCalls = new java.util.ArrayList<>(java.util.Collections.nCopies(11, 0));
        
        // 조회된 데이터 매핑
        for (Map<String, Object> data : hourlyData) {
            int hour = ((Number) data.get("HOUR")).intValue();
            int count = ((Number) data.get("CALL_COUNT")).intValue();
            String dataType = (String) data.get("DATA_TYPE");
            
            // 9시부터 19시까지만 처리
            if (hour >= 9 && hour <= 19) {
                int index = hour - 9; // 9시는 인덱스 0, 10시는 인덱스 1, ...
                if ("TODAY".equals(dataType)) {
                    todayCalls.set(index, count);
                } else if ("YESTERDAY".equals(dataType)) {
                    yesterdayCalls.set(index, count);
                }
            }
        }
        
        return new HourlyCallsDataDto(hours, todayCalls, yesterdayCalls);
    }
    
    // 월간 비교 데이터 계산 메서드 추가
    private YearlyComparisonDataDto calculateMonthlyComparison(String currentStartDate, String currentEndDate, String previousStartDate, String custCode) {
        YearlyComparisonDataDto comparisonData = new YearlyComparisonDataDto();

        try {
            // 이전 월의 종료일 계산
            LocalDate prevStart = LocalDate.parse(previousStartDate, YYYYMMDD_FORMATTER);
            LocalDate prevEnd = prevStart.withDayOfMonth(prevStart.lengthOfMonth());
            String previousEndDate = prevEnd.format(YYYYMMDD_FORMATTER);
            
            logger.info("월간 비교 계산 - 현재 기간: {} ~ {}, 이전 기간: {} ~ {}", 
                      currentStartDate, currentEndDate, previousStartDate, previousEndDate);
            
            // 현재 월 총 콜 수
            Integer currentTotalCalls = staticsMapper.getMonthlyTotalCallsForComparison(currentStartDate, currentEndDate, custCode);
            // 이전 월 총 콜 수
            Integer previousTotalCalls = staticsMapper.getMonthlyTotalCallsForComparison(previousStartDate, previousEndDate, custCode);
            // 현재 월 완료 콜 수
            Integer currentCompletedCalls = staticsMapper.getMonthlyCompletedCallsForComparison(currentStartDate, currentEndDate, custCode);
            // 이전 월 완료 콜 수
            Integer previousCompletedCalls = staticsMapper.getMonthlyCompletedCallsForComparison(previousStartDate, previousEndDate, custCode);

            // 현재 월 응답률 (백엔드에서 계산)
            Double currentCompletionRate = staticsMapper.getMonthlyCompletionRate(currentStartDate, currentEndDate, custCode);
            // 이전 월 응답률 (백엔드에서 계산)
            Double previousCompletionRate = staticsMapper.getMonthlyCompletionRate(previousStartDate, previousEndDate, custCode);

            // 값 저장
            comparisonData.setTotalCallsCurrentYear(currentTotalCalls != null ? currentTotalCalls : 0);
            comparisonData.setTotalCallsPreviousYear(previousTotalCalls != null ? previousTotalCalls : 0);
            comparisonData.setCmplCslCurrYr(currentCompletedCalls != null ? currentCompletedCalls : 0);
            comparisonData.setCmplCslPrevYr(previousCompletedCalls != null ? previousCompletedCalls : 0);
            comparisonData.setCurrentCompletionRate(currentCompletionRate != null ? currentCompletionRate : 0.0);
            comparisonData.setPreviousCompletionRate(previousCompletionRate != null ? previousCompletionRate : 0.0);
            
            // 증감률 계산
            comparisonData.setTotalCallsComparisonRate(calculateRate(currentTotalCalls, previousTotalCalls));
            comparisonData.setCompletedCounselingCallsComparisonRate(calculateRate(currentCompletedCalls, previousCompletedCalls));
            comparisonData.setCompletionRateComparisonRate(calculateRate(currentCompletionRate, previousCompletionRate));
            
            logger.info("Monthly comparison data: current total calls={}, previous total calls={}, current completed calls={}, previous completed calls={}",
                     currentTotalCalls, previousTotalCalls, currentCompletedCalls, previousCompletedCalls);
        } catch (Exception e) {
            logger.error("Error calculating monthly comparison data: {}", e.getMessage(), e);
        }
        
        return comparisonData;
    }
}