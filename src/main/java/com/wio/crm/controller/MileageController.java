package com.wio.crm.controller;

import com.wio.crm.model.Mileage;
import com.wio.crm.model.Transaction;
import com.wio.crm.service.DashboardService;
import com.wio.crm.service.MileageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class MileageController {

    @Autowired
    private MileageService mileageService;

    @GetMapping("/mileageStatus")
    public String showMileage(Model model) {
        Map<String, Object> mileageData = mileageService.getRemainingMileage();

        Mileage mileage = (Mileage) mileageData.get("remainingMileage");
        List<Transaction> transactions = (List<Transaction>) mileageData.get("mileageCharge");

        model.addAttribute("mileage", mileage);
        model.addAttribute("transactions", transactions);

        return "mileage/mileageStatus";
    }

}
