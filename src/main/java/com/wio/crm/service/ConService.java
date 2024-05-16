package com.wio.crm.service;

import com.wio.crm.config.CustomUserDetails;
import com.wio.crm.mapper.ConsMapper;
import com.wio.crm.model.Consultation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ConService {

    @Autowired
    private ConsMapper consMapper;

    private String getCurrentCustcode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ""; // Early return for null or incorrect type
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Using a ternary operator to simplify code
        return userDetails.getTcntUserInfo() != null ? userDetails.getTcntUserInfo().getCustCode() : "";
    }

    public List<Consultation> getConsultations(int page, int pageSize, String startDate, String endDate, String status, String type, String mall , String  keyword,String filter) {
        int offset = (page - 1) * pageSize+ 1;
        int limit = page * pageSize;
        Map<String, Object> params = new HashMap<>();
        System.out.println("getCurrentCustcode()===" + getCurrentCustcode());
        params.put("custCode", getCurrentCustcode());
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("status", status);
        params.put("type", type);
        params.put("mall", mall);
        params.put("keyword", keyword);
        params.put("filter", filter);
        params.put("offset", offset);
        params.put("limit", limit);


        return consMapper.selectList("selectList", params);
    }

    public int countTotal(String startDate, String endDate, String status, String type, String mall, String  keyword,String filter) {
        Map<String, Object> params = new HashMap<>();
        params.put("custCode", getCurrentCustcode());
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("status", status);
        params.put("keyword", keyword);
        params.put("filter", filter);
        params.put("type", type);
        params.put("mall", mall);

        return consMapper.countTotal(params);
    }
    public List<Consultation> getConsultationsForExcel(String startDate, String endDate, String status, String type, String mall, String filter, String keyword) {
        Map<String, Object> params = new HashMap<>();
        params.put("custCode", getCurrentCustcode());
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("status", status);
        params.put("type", type);
        params.put("mall", mall);
        params.put("filter", filter);
        params.put("keyword", keyword);

        return consMapper.selectAllForExcel(params);
    }

}

