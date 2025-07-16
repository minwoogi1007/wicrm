package com.wio.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 물류센터 직접 입고 관리 검색 조건 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogisticsDirectReturnSearchDTO {
    
    // 날짜 검색
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    // 기본 검색
    private String keyword;              // 통합 검색 (고객명, 제품코드, 운송장번호 등)
    private String siteName;             // 사이트명
    private String customerName;         // 고객명
    private String productCode;          // 제품코드
    private String trackingNumber;       // 운송장번호
    
    // 상태 검색
    private String processingStatus;     // 처리상태 (RECEIVED, PROCESSED)
    private String mappingStatus;        // 매핑상태 (PENDING, MATCHED, UNMATCHED)
    
    // 페이징
    private int page = 0;
    private int size = 20;
    private String sort = "id";
    private String direction = "desc";
    
    // 편의 메서드들
    
    /**
     * 검색 조건이 있는지 확인
     */
    public boolean hasSearchCriteria() {
        return (keyword != null && !keyword.trim().isEmpty()) ||
               (siteName != null && !siteName.trim().isEmpty()) ||
               (customerName != null && !customerName.trim().isEmpty()) ||
               (productCode != null && !productCode.trim().isEmpty()) ||
               (trackingNumber != null && !trackingNumber.trim().isEmpty()) ||
               (processingStatus != null && !processingStatus.trim().isEmpty()) ||
               (mappingStatus != null && !mappingStatus.trim().isEmpty()) ||
               startDate != null || endDate != null;
    }
    
    /**
     * 날짜 검색 조건이 있는지 확인
     */
    public boolean hasDateRange() {
        return startDate != null || endDate != null;
    }
    
    /**
     * 키워드 검색 조건이 있는지 확인
     */
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
    
    /**
     * 매핑 관련 검색 조건이 있는지 확인
     */
    public boolean hasMappingCriteria() {
        return (trackingNumber != null && !trackingNumber.trim().isEmpty()) ||
               (mappingStatus != null && !mappingStatus.trim().isEmpty());
    }
    
    /**
     * 검색 조건 초기화
     */
    public void clear() {
        this.keyword = null;
        this.siteName = null;
        this.customerName = null;
        this.productCode = null;
        this.trackingNumber = null;
        this.processingStatus = null;
        this.mappingStatus = null;
        this.startDate = null;
        this.endDate = null;
    }
    
    /**
     * 기본 정렬 설정
     */
    public void setDefaultSort() {
        this.sort = "id";
        this.direction = "desc";
    }
    
    /**
     * 날짜 범위 유효성 검증
     */
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true; // null인 경우는 유효함
        }
        return !startDate.isAfter(endDate);
    }
    
    /**
     * 정렬 방향 문자열 반환 (MyBatis용)
     */
    public String getSortDirection() {
        return "desc".equalsIgnoreCase(direction) ? "DESC" : "ASC";
    }
    
    /**
     * 페이징 오프셋 계산
     */
    public int getOffset() {
        return page * size;
    }
    
    /**
     * Oracle 11g 페이징용 endRow 계산
     */
    public int getEndRow() {
        return (page + 1) * size;
    }
} 