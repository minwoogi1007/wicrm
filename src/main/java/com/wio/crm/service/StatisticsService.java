package com.wio.crm.service;

import com.wio.crm.config.CustomUserDetails;
import com.wio.crm.mapper.StaticsMapper;
import com.wio.crm.model.DateRange;
import com.wio.crm.model.Statics;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class StatisticsService {


    private final StaticsMapper staticsMapper;

    public StatisticsService(StaticsMapper staticsMapper) {
        this.staticsMapper = staticsMapper;
    }


    private String getCurrentCustcode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ""; // Early return for null or incorrect type
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Using a ternary operator to simplify code
        return userDetails.getTcntUserInfo() != null ? userDetails.getTcntUserInfo().getCustCode() : "";
    }

    public List<Statics> getStatisticsCons( String start_date,  String end_date) {
        String custCode = getCurrentCustcode(); // 고객 코드 조회

        try {
            return staticsMapper.getStatisticsCons(start_date, end_date, custCode);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving statistics", e);
        }
    }
}