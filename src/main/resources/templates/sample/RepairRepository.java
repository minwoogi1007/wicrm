package com.wio.repairsystem.repository;

import com.wio.repairsystem.model.RepairRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepairRepository extends JpaRepository<RepairRequest, Long> {
    // 추가적인 쿼리 메소드가 필요한 경우 여기에 정의
}