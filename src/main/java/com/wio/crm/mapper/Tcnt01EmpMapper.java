package com.wio.crm.mapper;

import com.wio.crm.model.Tcnt01Emp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface Tcnt01EmpMapper {
    /**
     * 사용자 ID로 거래처 직원 정보를 조회합니다.
     * TCNT01 테이블과 LEFT JOIN하여 거래처 등급 정보를 함께 조회합니다.
     * @param username 사용자 ID
     * @return 거래처 직원 정보
     */
    Tcnt01Emp findByUserId(@Param("username") String username);

    /**
     * 거래처 직원 정보를 삽입합니다.
     * @param tcnt01Emp 삽입할 거래처 직원 정보
     */
    void insertTcnt01Emp(Tcnt01Emp tcnt01Emp);
}