package com.wio.crm.mapper;

import com.wio.crm.model.UserInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    @Insert("INSERT INTO N_TIPDW (ID,USERID, PW,GUBN,INSERT_TIME,CONFIRM_YN,USERNAME) VALUES (#{userId},#{userId}, #{password},'1',sysdate,'N', #{userName})")
    void insertUser(UserInfo userInfo);
}
