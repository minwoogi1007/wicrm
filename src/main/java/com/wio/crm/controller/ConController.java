package com.wio.crm.controller;

import com.wio.crm.model.Consultation;
import com.wio.crm.service.AdminCodeService;
import com.wio.crm.service.ConService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ConController {
    @Autowired
    private AdminCodeService adminCodeService;

    @Autowired
    private ConService conService;


    @GetMapping("/cons")
    public String consulting( Model model) {

        model.addAttribute("constat", adminCodeService.getAdminCodesByGubn("4003"));
        model.addAttribute("contype", adminCodeService.getAdminCodesByGubn("4002"));
        model.addAttribute("conbuy", adminCodeService.getAdminCodesByGubn("5000"));

        return "cons/consulting";
    }

    @GetMapping("/api/consultations")
    public ResponseEntity<Map<String, Object>> getConsultations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        List<Consultation> consultations = conService.getConsultations(page, pageSize, startDate, endDate);
        int total = conService.countTotal(startDate, endDate); // 전체 데이터 개수를 구하는 서비스 메소드

        Map<String, Object> response = new HashMap<>();
        response.put("data", consultations);
        response.put("total", total);

        return ResponseEntity.ok(response);
    }
}
