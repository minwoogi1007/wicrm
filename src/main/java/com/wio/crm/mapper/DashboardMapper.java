package com.wio.crm.mapper;


import com.wio.crm.model.DashboardData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {
    DashboardData findDataForCard1(String custCode);

    DashboardData findDataForCard2(String custCode);

    List<DashboardData> findPointList(String custCode);

    DashboardData dashConSum(String custCode);

    DashboardData findYesterdayDataForCard1(String custCode);
    DashboardData findWeeklyDataForCard1(String custCode);
    DashboardData findMonthlyDataForCard1(String custCode);
    
    DashboardData findYesterdayDataForCard2(String custCode);
    DashboardData findWeeklyDataForCard2(String custCode);
    DashboardData findMonthlyDataForCard2(String custCode);

    List<DashboardData> getDashboardCallCount(String custCode);

    List<DashboardData> getDashboardPersonCount(String custCode);

    DashboardData getDashboardMonth(String custCode);

    List<DashboardData> getEmployeeList();
    List<DashboardData> getCustomList();
    List<DashboardData> getDailyAve(String custCode);

    List<DashboardData> getWeeklySum(String custCode);
    List<DashboardData> getMonthlySum(String custCode);

    /**
     * 고객 코드를 기반으로 TCNT01 테이블에서 cust_grade 값을 조회합니다.
     * @param params custCode를 포함하는 매개변수 맵
     * @return 고객 등급 (예: 'A', 'B' 등) 또는 null
     */
    String getCustomerGrade(Map<String, Object> params);

    DashboardData getPoint(String custCode);

    DashboardData getCount(String custCode);

    List<DashboardData> getPointList(String custCode);

    List<DashboardData> getCountSum(String custCode);
    
    /**
     * 사용자 ID를 기반으로 N_TCNT01_EMP 테이블에서 authority 값을 조회합니다.
     * @param params userId를 포함하는 매개변수 맵
     * @return 사용자 권한 정보 또는 null
     */
    String getUserAuthority(Map<String, Object> params);

}
