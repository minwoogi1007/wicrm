package com.wio.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 교환/반품 검색 조건 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnItemSearchDTO {
    private String keyword;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
    
    private String returnTypeCode;
    private String returnStatusCode;
    private String siteName;
    private String paymentStatus;
    private String brandFilter; // 브랜드 필터 (RENOMA, CORALIC, OTHER)
    
    // 페이징 정보
    private int page = 0;
    private int size = 20;
    private String sortBy = "id";
    private String sortDir = "DESC";
    
    /**
     * 검색 조건이 있는지 확인
     */
    public boolean hasSearchCondition() {
        return (keyword != null && !keyword.trim().isEmpty()) ||
               startDate != null ||
               endDate != null ||
               (returnTypeCode != null && !returnTypeCode.trim().isEmpty()) ||
               (returnStatusCode != null && !returnStatusCode.trim().isEmpty()) ||
               (siteName != null && !siteName.trim().isEmpty()) ||
               (paymentStatus != null && !paymentStatus.trim().isEmpty()) ||
               (brandFilter != null && !brandFilter.trim().isEmpty());
    }
} 