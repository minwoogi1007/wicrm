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
import java.util.List;
import java.util.Map;

@Controller
public class StatController {

    @Autowired
    private StatisticsService statisticsService;
    @GetMapping("/statCons")
    public String statCons() {
        return "statistics/statCons";
    }

    @PostMapping("/statCons/searchCons")  // 리스트
    public String getConsultationStats(Model model,   @RequestParam("start_date") String startDate,
                                       @RequestParam("end_date") String endDate) {
        List<Statics> stats = statisticsService.getStatisticsCons(startDate, endDate);
        model.addAttribute("hidden_start_date", startDate);
        model.addAttribute("hidden_end_date", endDate);
        model.addAttribute("stats", stats);
        return "statistics/statCons"; // Ensure this points to the correct Thymeleaf template
    }
    @GetMapping("/api/statCons/searchCons")//그래프
    public ResponseEntity<Map<String, Object>> getStatisticsConsG(@RequestParam("start_date") String startDate,
                                                                    @RequestParam("end_date") String endDate) {

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
        List<Statics> stats = statisticsService.getConsultationResult(startDate, endDate);
        model.addAttribute("hidden_start_date", startDate);
        model.addAttribute("hidden_end_date", endDate);
        model.addAttribute("stats", stats);
        return "statistics/statResult"; // Ensure this points to the correct Thymeleaf template
    }
    @GetMapping("/api/statResult/searchResult")//그래프
    public ResponseEntity<Map<String, Object>> getConsultationResultG(@RequestParam("start_date") String startDate,
                                                                  @RequestParam("end_date") String endDate) {

        Map<String, Object> data = statisticsService.getConsultationResultG(startDate, endDate);
        return ResponseEntity.ok(data);
    }
    @GetMapping("/statTime")
    public String statTime() {
        return "statistics/statTime";
    }
    @GetMapping("/statCall")
    public String statCall() {
        return "statistics/statCall";
    }
}
