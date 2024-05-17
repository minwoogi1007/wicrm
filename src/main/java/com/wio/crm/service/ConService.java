package com.wio.crm.service;

import com.wio.crm.config.CustomUserDetails;
import com.wio.crm.mapper.ConsMapper;
import com.wio.crm.model.Comment;
import com.wio.crm.model.Consultation;
import com.wio.crm.model.History;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ConService {

    @Autowired
    private ConsMapper consMapper;
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ""; // Early return for null or incorrect type
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Using a ternary operator to simplify code
        return userDetails.getTcntUserInfo() != null ? userDetails.getTcntUserInfo().getUserId() : "";
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
    public Consultation getConsultationDetails( String projectCode, String personCode, String callCode) {

        Map<String, Object> params = new HashMap<>();
        params.put("custCode", getCurrentCustcode());
        params.put("projectCode", projectCode);
        params.put("personCode", personCode);
        params.put("callCode", callCode);
        return consMapper.selectConsultationDetails(params);
    }


    public List<Comment> getComments( String projectCode, String personCode, String callCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("custCode", getCurrentCustcode());
        params.put("projectCode", projectCode);
        params.put("personCode", personCode);
        params.put("callCode", callCode);
        return consMapper.getComments(params);
    }

    public List<History> getHistory(String projectCode, String personCode, String callCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("custCode", getCurrentCustcode());
        params.put("projectCode", projectCode);
        params.put("personCode", personCode);
        params.put("callCode", callCode);
        return consMapper.getHistory(params);
    }



    public void addComment(Comment request) {
        Comment comment = new Comment();
        comment.setUserId(getCurrentUserId());
        comment.setConText(request.getConText());
        comment.setCustCode(request.getCustCode());
        comment.setProjectCode(request.getProjectCode());
        comment.setPersonCode(request.getPersonCode());
        comment.setCallCode(request.getCallCode());
        consMapper.insertComment(comment);
    }


}

