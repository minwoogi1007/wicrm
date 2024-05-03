package com.wio.crm.mapper;

import com.wio.crm.model.DashboardData;
import com.wio.crm.model.Statics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StaticsMapper {

    List<Statics> getStatisticsCons(@Param("start") String start, @Param("end") String end, @Param("custCode") String custCode);
    List<Statics> getStatisticsConsG(@Param("start") String start, @Param("end") String end,@Param("custCode") String custCode);

}
