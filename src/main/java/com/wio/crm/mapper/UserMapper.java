package com.wio.crm.mapper;

import com.wio.crm.model.UserInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    @Insert("INSERT INTO N_TIPDW (ID,USERID, PW,GUBN,INSERT_TIME,CONFIRM_YN,USERNAME) VALUES (#{userId},#{userId}, #{password},#{gubn},sysdate,'N', #{userName})")
    void insertUser(UserInfo userInfo);

    @Insert("INSERT INTO N_TCNT01_EMP_TEMP (CUST_NAME,\n" +
            "            EMPNO,\n" +
            "            EMP_NAME,\n" +
            "            ID,\n" +
            "            USERID,\n" +
            "            PW,\n" +
            "            USE_YN,\n" +
            "            IN_DATE) VALUES (#{companyName},(SELECT 'T'|| LPAD( nvl(max(substr(EMPNO,2,9)),0)+1 ,9,0)  FROM tcnt01_emp_temp),#{userName},#{userId},#{userId}, #{password},'N',sysdate)")
    void insertTcnt01Emp (UserInfo userInfo);

    @Insert("INSERT INTO N_TEMP01  (\n" +
            "            EMPNO,\n" +
            "            SAUP_GUBN,\n" +
            "            EMP_NAME,\n" +
            "            ID,\n" +
            "            USERID,\n" +
            "            PW,\n" +
            "            CALL_NO,\n" +
            "            IN_DATE)  VALUES ( (SELECT 'T'|| LPAD( nvl(max(substr(EMPNO,2,9)),0)+1 ,9,0)  FROM temp01), '11',#{userName},#{userId},#{userId}, #{password},'0000',sysdate)")
    void insertTemp01 (UserInfo userInfo);
}
