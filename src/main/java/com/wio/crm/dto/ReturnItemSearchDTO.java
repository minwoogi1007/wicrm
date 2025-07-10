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
    
    // CS 접수일 검색 범위
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
    
    // 물류확인일 검색 범위
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate logisticsStartDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate logisticsEndDate;
    
    private String returnTypeCode;
    private String returnStatusCode;
    private String siteName;
    private String paymentStatus;
    private String brandFilter; // 브랜드 필터 (RENOMA, CORALIC, OTHER)
    private String filters; // 다중 필터 조건 (쉼표로 구분)
    
    // 페이징 정보
    private int page = 0;
    private int size = 20;
    private String sortBy = "id";
    private String sortDir = "DESC";
    
    /**
     * 검색 조건이 있는지 확인 (필터는 제외)
     */
    public boolean hasSearchCondition() {
        return (keyword != null && !keyword.trim().isEmpty() && !"null".equals(keyword.trim())) ||
               startDate != null ||
               endDate != null ||
               logisticsStartDate != null ||
               logisticsEndDate != null ||
               (returnTypeCode != null && !returnTypeCode.trim().isEmpty() && !"null".equals(returnTypeCode.trim())) ||
               (returnStatusCode != null && !returnStatusCode.trim().isEmpty() && !"null".equals(returnStatusCode.trim())) ||
               (siteName != null && !siteName.trim().isEmpty() && !"null".equals(siteName.trim())) ||
               (paymentStatus != null && !paymentStatus.trim().isEmpty() && !"null".equals(paymentStatus.trim())) ||
               (brandFilter != null && !brandFilter.trim().isEmpty() && !"null".equals(brandFilter.trim()));
               // 🚫 filters 조건 제거: 필터는 검색 조건이 아님
    }
    
    /**
     * sortDir getter - null 체크 추가
     */
    public String getSortDir() {
        return sortDir != null ? sortDir : "DESC";
    }
    
    /**
     * sortBy getter - null 체크 추가
     */
    public String getSortBy() {
        return sortBy != null ? sortBy : "id";
    }
    
    @Override
    public String toString() {
        return "ReturnItemSearchDTO{" +
                "keyword='" + keyword + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", logisticsStartDate=" + logisticsStartDate +
                ", logisticsEndDate=" + logisticsEndDate +
                ", returnTypeCode='" + returnTypeCode + '\'' +
                ", returnStatusCode='" + returnStatusCode + '\'' +
                ", siteName='" + siteName + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", brandFilter='" + brandFilter + '\'' +
                ", filters='" + filters + '\'' +
                ", page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", sortDir='" + sortDir + '\'' +
                ", hasSearchCondition=" + hasSearchCondition() +
                '}';
    }
} 