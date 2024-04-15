package com.wio.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatController {

    @GetMapping("/statCons")
    public String statCons() {
        return "statistics/statCons";
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
