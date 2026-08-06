package com.wio.crm.controller;

import com.wio.crm.config.CustomUserDetails;
import com.wio.crm.mapper.MileageMapper;
import com.wio.crm.model.Mileage;
import com.wio.crm.model.Transaction;
import com.wio.crm.service.MileageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class MileageController {

    private static final Logger log = LoggerFactory.getLogger(MileageController.class);

    @Autowired
    private MileageService mileageService;

    @Autowired
    private MileageMapper mileageMapper;

    @GetMapping("/mileageStatus")
    public String showMileage(Model model) {
        Map<String, Object> mileageData = mileageService.getRemainingMileage();

        Mileage mileage = (Mileage) mileageData.get("remainingMileage");
        List<Transaction> transactions = (List<Transaction>) mileageData.get("mileageCharge");

        model.addAttribute("mileage", mileage);
        model.addAttribute("transactions", transactions);

        return "mileage/mileageStatus";
    }

    @GetMapping("/mileage/consume")
    public String showConsumeHistory(Model model, Authentication authentication) {
        String custCode = "";
        String custName = "";

        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            if (userDetails.getTcntUserInfo() != null) {
                custCode = userDetails.getTcntUserInfo().getCustCode();
                custName = userDetails.getTcntUserInfo().getCust_name();
            }
        }

        List<Map<String, Object>> projects = new ArrayList<>();
        String remainingPoint = "0";
        if (custCode != null && !custCode.isEmpty()) {
            projects = mileageMapper.getProjectsByCustCode(custCode);
            remainingPoint = mileageMapper.getRemainingPointSum(custCode);
        }

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        model.addAttribute("custCode", custCode);
        model.addAttribute("custName", custName);
        model.addAttribute("projects", projects);
        model.addAttribute("remainingPoint", remainingPoint != null ? remainingPoint.trim() : "0");
        model.addAttribute("today", today);

        return "mileage/consume";
    }

    @GetMapping("/mileage/consume/search")
    @ResponseBody
    public Map<String, Object> searchConsumeHistory(
            @RequestParam(name = "projectCode", required = false) String projectCode,
            @RequestParam(name = "ioGubn", required = false) String ioGubn,
            @RequestParam(name = "startDate") String startDate,
            @RequestParam(name = "endDate") String endDate,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            String custCode = "";
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                if (userDetails.getTcntUserInfo() != null) {
                    custCode = userDetails.getTcntUserInfo().getCustCode();
                }
            }

            if (custCode == null || custCode.isEmpty()) {
                response.put("success", false);
                response.put("message", "거래처 정보가 없습니다.");
                return response;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("custCode", custCode);
            params.put("projectCode", projectCode);
            params.put("ioGubn", ioGubn);
            params.put("startDate", startDate);
            params.put("endDate", endDate);

            List<Map<String, Object>> list = mileageMapper.getPointHistory(params);
            String remainingPoint = mileageMapper.getRemainingPointSum(custCode);

            response.put("success", true);
            response.put("data", list);
            response.put("remainingPoint", remainingPoint != null ? remainingPoint.trim() : "0");
            response.put("totalCount", list.size());

        } catch (Exception e) {
            log.error("[MileageController] 포인트 사용 조회 실패", e);
            response.put("success", false);
            response.put("message", "조회 중 오류가 발생했습니다.");
        }

        return response;
    }

}
