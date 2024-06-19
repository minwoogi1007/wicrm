package com.wio.crm.mapper;

import com.wio.crm.model.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface MenuMapper {
    List<Map<String, Object>> findMenusByRole(@Param("username") String username, @Param("authority") String authority);

}
