package com.wio.crm.mapper;

import com.wio.crm.model.Mileage;
import com.wio.crm.model.Transaction;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface MileageMapper {
    Mileage getRemainingMileage(String custCode);
    List<Transaction> getAllTransactions(String custCode);
    
    List<Map<String, Object>> getProjectsByCustCode(String custCode);
    List<Map<String, Object>> getPointHistory(Map<String, Object> params);
    String getRemainingPointSum(String custCode);
}
