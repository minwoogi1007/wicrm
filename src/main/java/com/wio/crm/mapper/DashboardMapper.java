package com.wio.crm.mapper;


import com.wio.crm.model.DashboardData;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DashboardMapper {
    DashboardData findDataForCard1();

    DashboardData findDataForCard2();
}
