package com.wio.crm.mapper;

import com.wio.crm.model.UserApproval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserApprovalMapper {
    List<UserApproval> findByConfirmYn();
    UserApproval  findById(@Param("id") String id);
    void updateUserApproval(UserApproval userApproval);
}