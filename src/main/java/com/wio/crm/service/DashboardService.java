package com.wio.crm.service;

import com.wio.crm.mapper.DashboardMapper;
import com.wio.crm.model.DashboardData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        // 데이터베이스 조회
        DashboardData card1Data = dashboardMapper.findDataForCard1();
        DashboardData card2Data = dashboardMapper.findDataForCard2();
        List<DashboardData> pointList = dashboardMapper.findPointList(); //
        DashboardData dashConSum = dashboardMapper.dashConSum();


        data.put("card-data-1", card1Data);
        data.put("card-data-2", card2Data);
        data.put("card-data-3", dashConSum);
        data.put("pointlist-data", pointList);

        return data;
    }
    public Map<String, Object> getDashboardCallCount() {
        Map<String, Object> data = new HashMap<>();
        // 데이터베이스 조회
        List<DashboardData> getDashboardCallCount = dashboardMapper.getDashboardCallCount();

        data.put("dashStatCount-data", getDashboardCallCount);

        return data;
    }
}
