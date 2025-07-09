package com.wio.repairsystem.repository;

import com.wio.repairsystem.model.MainRequest;
import com.wio.repairsystem.model.ShippingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShippingOrderRepository extends JpaRepository<ShippingOrder, Long> {
    List<ShippingOrder> findByMainRequest(MainRequest mainRequest);
}