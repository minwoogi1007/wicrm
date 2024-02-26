package com.wio.crm.mapper;


import com.wio.crm.model.Board;
import com.wio.crm.model.DashboardData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DashboardMapper {
    DashboardData findDataForCard1(String username);

    DashboardData findDataForCard2(String username);

    List<DashboardData> findPointList(String username);

    DashboardData dashConSum(String username);

    List<DashboardData> getDashboardCallCount(String username);
}
