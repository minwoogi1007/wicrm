package com.wio.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConController {

    @GetMapping("/cons")
    public String consulting() {
        return "cons/consulting";
    }
}