package com.wio.crm.mapper;

import com.wio.crm.model.Order;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderMapper {
    void insertOrder(Order order);
    List<Order> findMissingInvoices();

    Long getNextOrderId();  // getNextOrderId 메서드 추가
    List<Order> getAllOrders();
}
