package com.wio.crm.service;

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
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // 권한 정보를 이용한 로직 구현
        for (GrantedAuthority authority : authorities) {
            System.out.println("================="+authority);
            if ("ROLE_EMPLOYEE".equals(authority.getAuthority())) {
                // ROLE_EMPLOYEE에 대한 로직 처리
                System.out.println("================="+authority.getAuthority());
            } else if ("ROLE_USER".equals(authority.getAuthority())) {
                // ROLE_USER에 대한 로직 처리
                System.out.println("================="+authority.getAuthority());
            }
        }

        Map<String, Object> data = new HashMap<>();
        // 데이터베이스 조회
        DashboardData card1Data = dashboardMapper.findDataForCard1(username);
        DashboardData card2Data = dashboardMapper.findDataForCard2(username);
        List<DashboardData> pointList = dashboardMapper.findPointList(username); //
        DashboardData dashConSum = dashboardMapper.dashConSum(username);


        data.put("card-data-1", card1Data);
        data.put("card-data-2", card2Data);
        data.put("card-data-3", dashConSum);
        data.put("pointlist-data", pointList);

        return data;
    }
    public Map<String, Object> getDashboardCallCount(String username) {
        Map<String, Object> data = new HashMap<>();
        // 데이터베이스 조회
        List<DashboardData> getDashboardCallCount = dashboardMapper.getDashboardCallCount(username);

        data.put("dashStatCount-data", getDashboardCallCount);

        return data;
    }
}
