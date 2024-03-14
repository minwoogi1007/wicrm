package com.wio.crm.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoginHistoryMapper {

    @Insert("INSERT INTO EMPLOGIN (ID, IN_DATE, SEQ) VALUES(#{userId}, SYSDATE, (SELECT TO_NUMBER(NVL(MAX(SEQ),0)+1) FROM EMPLOGIN WHERE TO_CHAR(IN_DATE,'YYYYMMDD') = TO_CHAR(SYSDATE,'YYYYMMDD') AND ID = #{userId}))")
    void insertLoginHistory(@Param("userId") String userId);
}