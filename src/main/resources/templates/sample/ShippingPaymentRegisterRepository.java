package com.wio.repairsystem.repository;

import com.wio.repairsystem.model.ShippingPaymentRegister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 배송비 입금 등록 Repository
 */
@Repository
public interface ShippingPaymentRegisterRepository extends JpaRepository<ShippingPaymentRegister, Long> {
    
    /**
     * 매핑 상태별 조회
     */
    Page<ShippingPaymentRegister> findByMappingStatus(String mappingStatus, Pageable pageable);
    List<ShippingPaymentRegister> findByMappingStatus(String mappingStatus, Sort sort);
    
    /**
     * 브랜드별 조회
     */
    Page<ShippingPaymentRegister> findByBrand(String brand, Pageable pageable);
    List<ShippingPaymentRegister> findByBrand(String brand, Sort sort);
    
    /**
     * 브랜드 및 매핑 상태별 조회
     */
    Page<ShippingPaymentRegister> findByBrandAndMappingStatus(String brand, String mappingStatus, Pageable pageable);
    
    /**
     * 고객명과 연락처로 매핑 가능한 교환/반품 찾기용 조회
     */
    @Query("SELECT spr FROM ShippingPaymentRegister spr WHERE " +
           "spr.customerName = :customerName AND spr.customerPhone = :customerPhone " +
           "AND spr.mappingStatus = 'PENDING'")
    List<ShippingPaymentRegister> findPendingByCustomerInfo(
        @Param("customerName") String customerName, 
        @Param("customerPhone") String customerPhone
    );
    
    /**
     * 자동 매핑을 위한 미매핑 입금 내역 조회
     */
    @Query("SELECT spr FROM ShippingPaymentRegister spr WHERE " +
           "spr.mappingStatus = 'PENDING' " +
           "ORDER BY spr.registerDate DESC")
    List<ShippingPaymentRegister> findAllPendingPayments();
    
    /**
     * 특정 교환/반품 ID와 연결된 입금 내역 조회
     */
    List<ShippingPaymentRegister> findByReturnItemId(Long returnItemId);
    
    /**
     * 입금일시 범위로 조회
     */
    @Query("SELECT spr FROM ShippingPaymentRegister spr WHERE " +
           "spr.paymentDate BETWEEN :startDateTime AND :endDateTime " +
           "ORDER BY spr.paymentDate DESC")
    Page<ShippingPaymentRegister> findByPaymentDateBetween(
        @Param("startDateTime") LocalDateTime startDateTime, 
        @Param("endDateTime") LocalDateTime endDateTime, 
        Pageable pageable
    );
    
    /**
     * 통합 검색 (고객명, 연락처, 사이트명)
     */
    @Query("SELECT spr FROM ShippingPaymentRegister spr WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(spr.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "spr.customerPhone LIKE CONCAT('%', :keyword, '%') OR " +
           "LOWER(spr.siteName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY spr.registerDate DESC")
    Page<ShippingPaymentRegister> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 브랜드별 통계 조회
     */
    @Query("SELECT spr.brand, spr.mappingStatus, COUNT(spr), SUM(spr.amount) " +
           "FROM ShippingPaymentRegister spr " +
           "GROUP BY spr.brand, spr.mappingStatus")
    List<Object[]> getStatsByBrandAndStatus();
    
    /**
     * 매핑 상태별 카운트
     */
    long countByMappingStatus(String mappingStatus);
    
    /**
     * 브랜드별 카운트
     */
    long countByBrand(String brand);
    
    /**
     * 오늘 등록된 입금 내역 카운트
     */
    @Query("SELECT COUNT(spr) FROM ShippingPaymentRegister spr WHERE " +
           "spr.registerDate >= :startOfDay AND spr.registerDate < :endOfDay")
    long countTodayRegistered(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
    
    /**
     * 오늘 매핑된 입금 내역 카운트 (매핑일 기준)
     */
    @Query("SELECT COUNT(spr) FROM ShippingPaymentRegister spr WHERE " +
           "spr.mappingStatus = 'MAPPED' AND " +
           "spr.mappedDate >= :startOfDay AND spr.mappedDate < :endOfDay")
    long countTodayMapped(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
    
    /**
     * 고객명으로 검색
     */
    Page<ShippingPaymentRegister> findByCustomerNameContaining(String customerName, Pageable pageable);
    List<ShippingPaymentRegister> findByCustomerNameContaining(String customerName, Sort sort);
    
    /**
     * 연락처로 검색
     */
    Page<ShippingPaymentRegister> findByCustomerPhoneContaining(String customerPhone, Pageable pageable);
    List<ShippingPaymentRegister> findByCustomerPhoneContaining(String customerPhone, Sort sort);
    
    /**
     * 사이트명으로 검색
     */
    Page<ShippingPaymentRegister> findBySiteNameContaining(String siteName, Pageable pageable);
    List<ShippingPaymentRegister> findBySiteNameContaining(String siteName, Sort sort);
    
    /**
     * 다중 필터 검색
     */
    @Query("SELECT spr FROM ShippingPaymentRegister spr WHERE " +
           "(:brand IS NULL OR :brand = '' OR spr.brand = :brand) AND " +
           "(:status IS NULL OR :status = '' OR spr.mappingStatus = :status) AND " +
           "(:customerName IS NULL OR :customerName = '' OR LOWER(spr.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
           "(:customerPhone IS NULL OR :customerPhone = '' OR spr.customerPhone LIKE CONCAT('%', :customerPhone, '%')) AND " +
           "(:siteName IS NULL OR :siteName = '' OR LOWER(spr.siteName) LIKE LOWER(CONCAT('%', :siteName, '%'))) " +
           "ORDER BY spr.registerDate DESC")
    Page<ShippingPaymentRegister> findByMultipleFilters(
        @Param("brand") String brand,
        @Param("status") String status,
        @Param("customerName") String customerName,
        @Param("customerPhone") String customerPhone,
        @Param("siteName") String siteName,
        Pageable pageable
    );
} 