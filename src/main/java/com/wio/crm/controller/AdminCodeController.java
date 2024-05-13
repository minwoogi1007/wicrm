package com.wio.crm.controller;

import com.wio.crm.service.AdminCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminCodeController {
    @Autowired
    private AdminCodeService adminCodeService;

    @GetMapping("/admin-codes")
    public String showAdminCodes(@RequestParam String admGubn, Model model) {
        model.addAttribute("adminCodes", adminCodeService.getAdminCodesByGubn(admGubn));
        return "adminCodesView";
    }

    @GetMapping("/admin-codes/4003")
    public String showAdminCodesFor4003(Model model) {
        model.addAttribute("adminCodes", adminCodeService.getAdminCodesByGubn("4003"));
        return "adminCodesView";
    }
}