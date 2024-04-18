package com.wio.crm.mapper;

import com.wio.crm.model.Tcnt01Emp;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper {
    Tcnt01Emp getAccount(String userId);
}
