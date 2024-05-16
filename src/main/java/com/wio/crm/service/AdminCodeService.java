package com.wio.crm.service;

import com.wio.crm.Entity.AdminCode;
import com.wio.crm.mapper.AdminCodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminCodeService {
    @Autowired
    private AdminCodeMapper adminCodeMapper;

    public List<AdminCode> getAdminCodesByGubn(String admGubn) {
        return adminCodeMapper.findByAdmGubn(admGubn);
    }
}