package com.wio.crm.controller;

import com.wio.crm.model.DateRange;
import com.wio.crm.model.Statics;
import com.wio.crm.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

    @GetMapping("/api/searchCons")
    @ResponseBody
    public List<Statics> getConsultationStats(@RequestParam String start_date, @RequestParam String end_date) {
        // Fetch data based on the dates
        List<Statics> stats = statisticsService.getStatisticsCons(start_date, end_date);
        return stats;
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
