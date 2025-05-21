package com.wio.crm.controller;

import com.wio.crm.model.DateRange;
import com.wio.crm.model.Statics;
import com.wio.crm.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
public class StatController {

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
}
