package com.wio.crm.mapper;

import com.wio.crm.model.Consultation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
@Mapper
public interface ConsMapper {

    /**
     * 상담 목록 조회
     * @param s 쿼리 ID
     * @param params 조회에 필요한 파라미터
     * @return 상담 서비스 목록
     */
    List<Consultation> selectList(@Param("s") String s, @Param("params") Map<String, Object> params);

    int countTotal(@Param("params") Map<String, Object> params);

    List<Consultation> selectAllForExcel( @Param("params") Map<String, Object> params);
}
