package com.wio.crm.mapper;

import com.wio.crm.model.Tipdw;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TipdwMapper {


    @Select("SELECT * FROM N_TIPDW WHERE userid = #{userid}")
    Tipdw findByUserId(@Param("userid") String userid);

    @Update("UPDATE N_TIPDW SET pw = #{user.pw} WHERE id = #{user.id}")
    void save(@Param("user") Tipdw user);

    @Select("SELECT * FROM N_TIPDW")
    List<Tipdw> findAll();

    // userId가 존재하는지 여부를 확인하는 쿼리
    @Select("SELECT COUNT(*) FROM N_TIPDW WHERE userid = #{userId}")
    int countByUserId(@Param("userId") String userId);
}
