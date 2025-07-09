package com.wio.crm.mapper;

import com.wio.crm.dto.ShippingPaymentRegisterDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 배송비 입금 매퍼
 */
@Mapper
public interface ShippingPaymentMapper {

    /**
     * 배송비 입금 등록
     */
    void insertPayment(ShippingPaymentRegisterDTO dto);

    /**
     * ID로 조회
     */
    ShippingPaymentRegisterDTO selectById(@Param("registerId") Long registerId);

    /**
     * 오늘 등록된 입금 건수 조회
     */
    Long selectTodayCount(@Param("today") LocalDate today);

    /**
     * 최근 입금 내역 조회
     */
    List<Map<String, Object>> selectRecentPayments(@Param("limit") int limit);

    /**
     * 입금 내역 목록 조회 (페이징)
     */
    List<Map<String, Object>> selectPaymentList(@Param("searchParams") Map<String, Object> searchParams);

    /**
     * 입금 내역 총 개수
     */
    Long selectPaymentCount(@Param("searchParams") Map<String, Object> searchParams);

    /**
     * 입금 내역 삭제
     */
    int deletePayment(@Param("registerId") Long registerId);

    /**
     * 입금 내역 수정
     */
    int updatePayment(ShippingPaymentRegisterDTO dto);

    /**
     * 교환 건과 매핑
     */
    int updateMapping(@Param("registerId") Long registerId, 
                     @Param("returnItemId") Long returnItemId,
                     @Param("mappingStatus") String mappingStatus);

    /**
     * 매핑 해제
     */
    int unmapPayment(@Param("registerId") Long registerId);
} 