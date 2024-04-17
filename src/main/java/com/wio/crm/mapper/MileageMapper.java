package com.wio.crm.mapper;

import com.wio.crm.model.DashboardData;
import com.wio.crm.model.Mileage;
import com.wio.crm.model.Transaction;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MileageMapper {
    Mileage getRemainingMileage(String custCode);
    List<Transaction> getAllTransactions(String custCode);
}
