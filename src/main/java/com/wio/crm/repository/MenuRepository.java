package com.wio.crm.repository;

import com.wio.crm.model.Menu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MenuRepository{
    List<Menu> findMenusByRole(String role);
}
