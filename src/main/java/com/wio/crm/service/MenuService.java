package com.wio.crm.service;

import com.wio.crm.model.Menu;
import com.wio.crm.mapper.MenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MenuService {

    @Autowired
    private MenuMapper menuMapper;


    public List<Map<String, Object>> getCompanyUserMenus(String role, String authority) {
        return menuMapper.findMenusByRole(role,authority);
    }
}
