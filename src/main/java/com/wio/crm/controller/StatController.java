package com.wio.crm.controller;

import com.wio.crm.model.DateRange;
import com.wio.crm.model.Statics;
import com.wio.crm.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/statCons/searchCons")
    public String getConsultationStats(Model model,   @RequestParam("start_date") String startDate,
                                       @RequestParam("end_date") String endDate) {
        List<Statics> stats = statisticsService.getStatisticsCons(startDate, endDate);
        model.addAttribute("stats", stats);
        return "statistics/statCons"; // Ensure this points to the correct Thymeleaf template
    }

    @GetMapping("/statResult")
    public String statResult() {
        return "statistics/statResult";
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
