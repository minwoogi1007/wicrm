package com.wio.crm.mapper;

import com.wio.crm.model.Banner;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface BannerMapper {
    @Select("SELECT * FROM banner WHERE active = 1 AND position = #{position} " +
            "AND (start_date IS NULL OR start_date <= SYSDATE) " +
            "AND (end_date IS NULL OR end_date >= SYSDATE) " +
            "ORDER BY display_order")
    List<Banner> findActiveBannersByPosition(String position);
    
    @Select("SELECT * FROM banner ORDER BY position, display_order")
    List<Banner> findAllBanners();
    
    @Select("SELECT * FROM banner WHERE id = #{id}")
    Banner findById(Long id);
    
    @Insert("INSERT INTO banner (name, image_url, link_url, position, active, display_order, start_date, end_date) " +
            "VALUES (#{name}, #{imageUrl}, #{linkUrl}, #{position}, " +
            "#{active}, #{displayOrder}, #{startDate, jdbcType=DATE}, #{endDate, jdbcType=DATE})")
    void insert(Banner banner);
    
    @Update("UPDATE banner SET name = #{name}, image_url = #{imageUrl}, " +
            "link_url = #{linkUrl}, position = #{position}, active = #{active}, " +
            "display_order = #{displayOrder}, start_date = #{startDate, jdbcType=DATE}, " +
            "end_date = #{endDate, jdbcType=DATE} WHERE id = #{id}")
    void update(Banner banner);
    
    @Delete("DELETE FROM banner WHERE id = #{id}")
    void deleteById(Long id);
}