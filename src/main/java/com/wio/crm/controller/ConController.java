package com.wio.crm.controller;

import com.wio.crm.service.AdminCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConController {
    @Autowired
    private AdminCodeService adminCodeService;
    @GetMapping("/cons")

    public String consulting( Model model) {

        model.addAttribute("constat", adminCodeService.getAdminCodesByGubn("4003"));
        model.addAttribute("contype", adminCodeService.getAdminCodesByGubn("4002"));
        model.addAttribute("conbuy", adminCodeService.getAdminCodesByGubn("5000"));

        return "cons/consulting";
    }
}
