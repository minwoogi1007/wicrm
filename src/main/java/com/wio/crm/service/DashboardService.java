package com.wio.crm.service;

import com.wio.crm.config.CustomUserDetails;
import com.wio.crm.mapper.DashboardMapper;
import com.wio.crm.model.DashboardData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class DashboardService {

    private final DashboardMapper dashboardMapper;

    @Autowired
    public DashboardService(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }
    public Map<String, Object> getDashboardData(String username) {

        System.out.println("================="+username);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 현재 사용자의 CustomUserDetails 객체에서 custCode 추출
        String custCode = null;
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            custCode = userDetails.getCustCode();
        }else{
            custCode = "";
        }
        System.out.println("custCode================="+custCode);
        Map<String, Object> data = new HashMap<>();
        // 데이터베이스 조회
        DashboardData card1Data = dashboardMapper.findDataForCard1(custCode);
        DashboardData card2Data = dashboardMapper.findDataForCard2(custCode);
        List<DashboardData> pointList = dashboardMapper.findPointList(custCode); //
        DashboardData dashConSum = dashboardMapper.dashConSum(custCode);

        System.out.println(authentication );
        data.put("card-data-1", card1Data);
        data.put("card-data-2", card2Data);
        data.put("card-data-3", dashConSum);
        data.put("pointlist-data", pointList);

        return data;
    }
    //로그인 유저별 코드 (거래처 는 업체 코드)
    private String getCurrentUserCustCode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getCustCode();
        }
        System.out.println("권한 --"+authentication.getAuthorities());
        return "";
    }
    public Map<String, Object> getDashboardCallCount(String username) {
        String custCode = getCurrentUserCustCode();
        Map<String, Object> data = new HashMap<>();
        data.put("dashStatCount-data", dashboardMapper.getDashboardCallCount(custCode));
        return data;
    }

    public Map<String, Object> getDashboardPersonCount(String username) {
        String custCode = getCurrentUserCustCode();
        Map<String, Object> data = new HashMap<>();
        data.put("dashStatCount-data", dashboardMapper.getDashboardPersonCount(custCode));
        return data;
    }

    public Map<String, Object> getDashboardMonth(String username) {
        String custCode = getCurrentUserCustCode();
        Map<String, Object> data = new HashMap<>();
        data.put("dashMonth-data", dashboardMapper.getDashboardMonth(custCode));
        return data;
    }
    public Map<String, Object> getEmployeeList() {

        Map<String, Object> data = new HashMap<>();
        data.put("employeeList", dashboardMapper.getEmployeeList());
        return data;
    }

    public Map<String, Object> getDailyAve(String username) {
        String custCode = getCurrentUserCustCode();
        Map<String, Object> data = new HashMap<>();
        data.put("dailyAve", dashboardMapper.getDailyAve(custCode));
        return data;
    }


}
