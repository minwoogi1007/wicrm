package com.wio.crm.mapper;

import com.wio.crm.Entity.AdminCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminCodeMapper {
    @Select("SELECT adm_code as admCode,adm_gubn as admGubn ,adm_sname as admSname FROM TSYS01 WHERE ADM_GUBN = #{admGubn}")
    List<AdminCode> findByAdmGubn(String admGubn);
}
