package com.wio.crm.mapper;

import com.wio.crm.model.Tcnt01Emp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface Tcnt01EmpMapper {
    @Select("SELECT CUST_CODE as custCode, (SELECT CUST_GRADE  FROM TCNT01 WHERE CUST_CODE = A.CUST_CODE) CUST_GRADE, EMPNO, EMP_NAME, ID, PW, DEPART, POSITION, ZIP_NO, ADDR, TEL_NO, FEX_NO, HAND_PHONE, EMAIL, RMK, USE_YN, IN_DATE, IN_EMPNO, UP_DATE, UP_EMPNO, ADDR2, SUBID, USERID FROM N_TCNT01_EMP A WHERE id = #{username}")
    Tcnt01Emp findByUserId(@Param("username") String username);
}