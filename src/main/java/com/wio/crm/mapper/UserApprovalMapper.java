package com.wio.crm.mapper;

import com.wio.crm.model.UserApproval;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserApprovalMapper {

    List<UserApproval> findAllApprovals();
    List<UserApproval> findByConfirmYn(String status);
    UserApproval  findById(@Param("id") String id);
    void updateUserApproval(UserApproval userApproval);

    // Update N_TIPDW table
    @Update("UPDATE N_TIPDW SET CONFIRM_YN = 'Y' WHERE USERID = #{userId}")
    void updateConfirmYn(@Param("userId") String userId);

    // Retrieve data from N_TCNT01_EMP_TEMP
    @Select("SELECT EMPNO, EMP_NAME, ID, PW, DEPART, POSITION, ZIP_NO, ADDR, TEL_NO, FEX_NO, HAND_PHONE, EMAIL, RMK, USE_YN, IN_DATE, IN_EMPNO, UP_DATE, UP_EMPNO, CUST_NAME, ADDR2, USERID FROM N_TCNT01_EMP_TEMP WHERE USERID = #{userId}")
    Map<String, Object> getEmpTempData(@Param("userId") String userId);

    // Generate new EMPNO
    @Select("SELECT 'E'|| LPAD( nvl(max(substr(EMPNO,2,9)),0)+1 ,9,0) FROM N_TCNT01_EMP")
    String generateNewEmpNo();

    // Insert data into N_TCNT01_EMP

    void insertEmpData(Map<String, Object> empData);

    @Select("SELECT CUST_CODE as code, CUST_NAME as name FROM TCNT01 where use_yn='1' ORDER BY CUST_CODE DESC")
    List<Map<String, String>> getCompanyList();

    @Update("UPDATE N_TIPDW SET CONFIRM_YN = 'N' WHERE USERID = #{userId}")
    void updateApprovalStatus(@Param("userId") String userId);
    @Delete("Delete FROM N_TCNT01_EMP WHERE USERID = #{userId}")
    void deleteEmpTempData (@Param("userId") String userId);
}