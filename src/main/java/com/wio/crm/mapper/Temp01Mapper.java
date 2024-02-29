package com.wio.crm.mapper;

import com.wio.crm.model.Temp01;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface Temp01Mapper {
    @Select("SELECT * FROM N_TEMP01 WHERE userid = #{username}")
    Temp01 findByUserId(@Param("username") String userid);
}
