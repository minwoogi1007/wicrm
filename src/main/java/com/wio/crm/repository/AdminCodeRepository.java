package com.wio.crm.repository;

import com.wio.crm.Entity.AdminCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminCodeRepository extends JpaRepository<AdminCode, String> {
    List<AdminCode> findByAdmGubn(String admGubn);
}