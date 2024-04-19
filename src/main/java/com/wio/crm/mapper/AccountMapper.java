package com.wio.crm.mapper;

import com.wio.crm.model.Tcnt01Emp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountMapper {
    Tcnt01Emp getAccount(String userId);

    void updateAccount(@Param("account") Tcnt01Emp account);

}
