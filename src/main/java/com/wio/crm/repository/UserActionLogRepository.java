package com.wio.crm.repository;

import com.wio.crm.Entity.UserActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActionLogRepository extends JpaRepository<UserActionLog, Long> {
    List<UserActionLog> findByUserIdOrderByActionTimestampDesc(String userId);
    List<UserActionLog> findByActionTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<UserActionLog> findByActionTypeAndUserIdOrderByActionTimestampDesc(String actionType, String userId);
} 