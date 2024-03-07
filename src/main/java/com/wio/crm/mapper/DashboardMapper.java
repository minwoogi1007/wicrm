package com.wio.crm.mapper;


import com.wio.crm.model.Board;
import com.wio.crm.model.DashboardData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DashboardMapper {
    DashboardData findDataForCard1(String custCode);

    DashboardData findDataForCard2(String custCode);

    List<DashboardData> findPointList(String custCode);

    DashboardData dashConSum(String custCode);

    List<DashboardData> getDashboardCallCount(String custCode);

    List<DashboardData> getDashboardPersonCount(String custCode);

    DashboardData getDashboardMonth(String custCode);


}
