package com.wio.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MileageController {
    @GetMapping("/mileageStatus")
    public String mileageStatus() {
        return "mileage/mileageStatus";
    }
}
