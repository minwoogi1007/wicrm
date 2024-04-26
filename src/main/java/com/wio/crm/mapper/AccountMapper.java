package com.wio.crm.mapper;

import com.wio.crm.model.Account;
import com.wio.crm.model.Tcnt01Emp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountMapper {
    Tcnt01Emp getAccount(String userId);

    int updateAccount(Account account);

    Tcnt01Emp findUserByUsername(String userId);
    void updateUserPassword(String userId, String password);

}
