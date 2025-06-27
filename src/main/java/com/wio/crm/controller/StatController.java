package com.wio.crm.controller;

import com.wio.crm.model.DateRange;
import com.wio.crm.model.Statics;
import com.wio.crm.service.StatisticsService;
import com.wio.crm.dto.DailyOperationStatsResponseDto;
import com.wio.crm.dto.WeeklyOperationStatsResponseDto;
import com.wio.crm.dto.MonthlyOperationStatsResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Controller
public class StatController {
    private static final Logger logger = LoggerFactory.getLogger(StatController.class);

    @Autowired
    private StatisticsService statisticsService;
    
    // 새로 추가된 일일/주간/월간 운영현황 컨트롤러
    @GetMapping("/stat/daily")
    public String dailyOperationStat(Model model) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        model.addAttribute("today", today.format(formatter));
        return "statistics/daily_operation";
    }

    @GetMapping("/stat/weekly")
    public String weeklyOperationStat(Model model) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        model.addAttribute("startDate", startOfWeek.format(formatter));
        model.addAttribute("endDate", endOfWeek.format(formatter));
        return "statistics/weekly_operation";
    }

    @GetMapping("/stat/monthly")
    public String monthlyOperationStat(Model model) {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        model.addAttribute("startDate", firstDayOfMonth.format(formatter));
        model.addAttribute("endDate", lastDayOfMonth.format(formatter));
        return "statistics/monthly_operation";
    }
    
    @GetMapping("/statCons")
    public String statCons() {
        return "statistics/statCons";
    }

    @PostMapping("/statCons/searchCons")  // 리스트
    public String getConsultationStats(Model model,   @RequestParam("start_date") String startDate,
                                       @RequestParam("end_date") String endDate) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (startDate == null || startDate.isEmpty()) {
            startDate = today.format(formatter);
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = today.format(formatter);
        }


        List<Statics> stats = statisticsService.getStatisticsCons(startDate, endDate);
        model.addAttribute("hidden_start_date", startDate);
        model.addAttribute("hidden_end_date", endDate);
        model.addAttribute("stats", stats);
        return "statistics/statCons"; // Ensure this points to the correct Thymeleaf template
    }
    @GetMapping("/api/statCons/searchCons")//그래프
    public ResponseEntity<Map<String, Object>> getStatisticsConsG(@RequestParam("start_date") String startDate,
                                                                    @RequestParam("end_date") String endDate) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (startDate == null || startDate.isEmpty()) {
            startDate = today.format(formatter);
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = today.format(formatter);
        }


        Map<String, Object> data = statisticsService.getStatisticsConsG(startDate, endDate);
        return ResponseEntity.ok(data);
    }
    @GetMapping("/statResult")
    public String statResult() {
        return "statistics/statResult";
    }

    @PostMapping("/statResult/searchResult")  // 리스트
    public String getConsultationResult(Model model,   @RequestParam("start_date") String startDate,
                                       @RequestParam("end_date") String endDate) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (startDate == null || startDate.isEmpty()) {
            startDate = today.format(formatter);
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = today.format(formatter);
        }

        List<Statics> stats = statisticsService.getConsultationResult(startDate, endDate);
        model.addAttribute("hidden_start_date", startDate);
        model.addAttribute("hidden_end_date", endDate);
        model.addAttribute("stats", stats);
        return "statistics/statResult"; // Ensure this points to the correct Thymeleaf template
    }
    @GetMapping("/api/statResult/searchResult")//그래프
    public ResponseEntity<Map<String, Object>> getConsultationResultG(@RequestParam("start_date") String startDate,
                                                                  @RequestParam("end_date") String endDate) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (startDate == null || startDate.isEmpty()) {
            startDate = today.format(formatter);
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = today.format(formatter);
        }

        Map<String, Object> data = statisticsService.getConsultationResultG(startDate, endDate);
        return ResponseEntity.ok(data);
    }
    @GetMapping("/statTime")
    public String statTime() {
        return "statistics/statTime";
    }

    @PostMapping("/statTime/searchTime")  // 리스트
    public String getConsultationTime(Model model,   @RequestParam("start_date") String startDate,
                                        @RequestParam("end_date") String endDate) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (startDate == null || startDate.isEmpty()) {
            startDate = today.format(formatter);
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = today.format(formatter);
        }


        List<Statics> stats = statisticsService.getConsultationTime(startDate, endDate);
        model.addAttribute("hidden_start_date", startDate);
        model.addAttribute("hidden_end_date", endDate);
        model.addAttribute("stats", stats);
        return "statistics/statTime"; // Ensure this points to the correct Thymeleaf template
    }
    @GetMapping("/api/statTime/searchTime")//그래프
    public ResponseEntity<Map<String, Object>> getConsultationTimeG(@RequestParam("start_date") String startDate,
                                                                      @RequestParam("end_date") String endDate) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (startDate == null || startDate.isEmpty()) {
            startDate = today.format(formatter);
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = today.format(formatter);
        }



        Map<String, Object> data = statisticsService.getConsultationTimeG(startDate, endDate);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/statCall")
    public String statCall() {
        return "statistics/statCall";
    }

    @PostMapping("/stat/daily/search")
    @ResponseBody
    public ResponseEntity<DailyOperationStatsResponseDto> searchDailyOperationStats(@RequestParam("date") String date) {
        logger.info("Received request for daily operation stats with date: {}", date);
        try {
            // 날짜 형식 검증
            if (date == null || date.isEmpty()) {
                logger.error("날짜가 비어있습니다.");
                return ResponseEntity.badRequest().body(null);
            }

            // 숫자로만 구성되어 있는지 확인 (YYYYMMDD 형식)
            if (!date.matches("^\\d{8}$")) {
                logger.error("잘못된 날짜 형식: {}", date);
                return ResponseEntity.badRequest().body(null);
            }

            // 유효한 날짜인지 추가 검증 (예: 20250231 같은 경우)
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                LocalDate.parse(date, formatter); // 파싱만 시도하여 유효성 검사
            } catch (DateTimeParseException e) {
                logger.error("유효하지 않은 날짜 값: {}", date, e);
                return ResponseEntity.badRequest().body(null);
            }
            
            DailyOperationStatsResponseDto statsDto = statisticsService.getDailyOperationStatistics(date);
            return ResponseEntity.ok(statsDto);
        } catch (IllegalStateException e) {
            logger.error("Error fetching daily operation stats due to illegal state (e.g., auth issue) for date {}: {}", date, e.getMessage());
            // IllegalStateException은 getCurrentCustCode에서 사용자 인증 문제 시 발생 가능
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // 또는 적절한 오류 DTO
        } catch (DateTimeParseException e) {
            logger.error("Error fetching daily operation stats for date {}: {}", date, e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        } catch (RuntimeException e) {
            logger.error("Error fetching daily operation stats for date {}: {}", date, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 또는 적절한 오류 DTO
        }
    }

    @PostMapping("/stat/weekly/search")
    @ResponseBody
    public ResponseEntity<WeeklyOperationStatsResponseDto> searchWeeklyOperationStats(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        logger.info("Received request for weekly operation stats with startDate: {} and endDate: {}", startDate, endDate);
        try {
            // 날짜 형식 변환: "2024-05-26" -> "20240526"
            String dbFormatStartDate = startDate.replaceAll("-", "");
            String dbFormatEndDate = endDate.replaceAll("-", "");
            
            WeeklyOperationStatsResponseDto statsDto = statisticsService.getWeeklyOperationStatistics(dbFormatStartDate, dbFormatEndDate);
            return ResponseEntity.ok(statsDto);
        } catch (IllegalStateException e) {
            logger.error("Error fetching weekly operation stats due to illegal state (e.g., auth issue) for date range {} - {}: {}", startDate, endDate, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (RuntimeException e) {
            logger.error("Error fetching weekly operation stats for date range {} - {}: {}", startDate, endDate, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/stat/monthly/search")
    @ResponseBody
    public ResponseEntity<MonthlyOperationStatsResponseDto> searchMonthlyOperationStats(@RequestParam("yearMonth") String yearMonth) {
        logger.info("Received request for monthly operation stats with yearMonth: {}", yearMonth);
        try {
            // yearMonth 형식: "2024-02"
            String startDate = yearMonth + "-01";
            
            // 해당 월의 마지막 날짜 계산
            YearMonth ym = YearMonth.parse(yearMonth);
            String endDate = yearMonth + "-" + ym.lengthOfMonth();
            
            // YYYYMMDD 형식으로 변환
            String dbFormatStartDate = startDate.replaceAll("-", "");
            String dbFormatEndDate = endDate.replaceAll("-", "");
            
            logger.info("월간 통계 조회 변환 날짜 - 시작일: {}, 종료일: {}", dbFormatStartDate, dbFormatEndDate);
            
            MonthlyOperationStatsResponseDto statsDto = statisticsService.getMonthlyOperationStatistics(dbFormatStartDate, dbFormatEndDate);
            
            // 디버깅: 주차별 통계 데이터 확인
            if (statsDto != null) {
                if (statsDto.getWeeklyStatsList() != null) {
                    logger.info("주차별 통계 데이터 수: {}", statsDto.getWeeklyStatsList().size());
                    for (int i = 0; i < statsDto.getWeeklyStatsList().size(); i++) {
                        logger.info("주차 데이터 {}: {}", i+1, statsDto.getWeeklyStatsList().get(i));
                    }
                } else {
                    logger.warn("주차별 통계 데이터가 null입니다.");
                }
            } else {
                logger.warn("월간 통계 응답 DTO가 null입니다.");
            }
            
            return ResponseEntity.ok(statsDto);
        } catch (IllegalStateException e) {
            logger.error("Error fetching monthly operation stats due to illegal state (e.g., auth issue) for yearMonth {}: {}", yearMonth, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (RuntimeException e) {
            logger.error("Error fetching monthly operation stats for yearMonth {}: {}", yearMonth, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
