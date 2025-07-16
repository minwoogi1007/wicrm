package com.wio.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 물류센터 직접 입고 관리 DTO
 * 실제 TB_LOGISTICS_DIRECT_RETURN 테이블 구조와 일치
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogisticsDirectReturnDTO {
    
    private Long id;
    private LocalDate receivedDate;
    private String siteName;
    private String customerName;
    private String customerPhone;
    
    // 상품 정보
    private String productCode;
    private String productColor;
    private String productSize;
    private Integer quantity;
    
    // 배송 정보
    private String trackingNumber;
    private String courierCompany;
    
    // 처리 정보
    private String remarks;
    private String processingStatus;
    
    // 매핑 정보
    private Long matchedReturnId;
    private String mappingStatus;
    private LocalDateTime mappingDate;
    private String mappingBy;
    
    // 시스템 관리
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String createdBy;
    private String updatedBy;
    
    // 매핑된 교환반품 정보 (조인 데이터)
    private String matchedOrderNumber;      // 매핑된 주문번호
    private String matchedCustomerName;     // 매핑된 고객명
    private String matchedReturnTypeCode;   // 매핑된 교환반품 유형
    private LocalDate matchedCsReceivedDate; // 매핑된 CS접수일
    
    // 편의 메서드들
    
    /**
     * 처리 상태 텍스트 반환
     */
    public String getProcessingStatusText() {
        if (this.processingStatus == null) {
            return "";
        }
        switch (this.processingStatus) {
            case "RECEIVED": return "입고완료";
            case "PROCESSED": return "처리완료";
            default: return this.processingStatus;
        }
    }
    
    /**
     * 매핑 상태 텍스트 반환
     */
    public String getMappingStatusText() {
        if (this.mappingStatus == null) {
            return "";
        }
        switch (this.mappingStatus) {
            case "PENDING": return "미매핑";
            case "MATCHED": return "매핑완료";
            case "UNMATCHED": return "매핑불가";
            default: return this.mappingStatus;
        }
    }
    
    /**
     * 매핑 상태 CSS 클래스 반환
     */
    public String getMappingStatusClass() {
        if (this.mappingStatus == null) {
            return "badge-secondary";
        }
        switch (this.mappingStatus) {
            case "PENDING": return "badge-warning";
            case "MATCHED": return "badge-success";
            case "UNMATCHED": return "badge-danger";
            default: return "badge-secondary";
        }
    }
    
    /**
     * 처리 상태 CSS 클래스 반환
     */
    public String getProcessingStatusClass() {
        if (this.processingStatus == null) {
            return "badge-secondary";
        }
        switch (this.processingStatus) {
            case "RECEIVED": return "badge-primary";
            case "PROCESSED": return "badge-success";
            default: return "badge-secondary";
        }
    }
    
    /**
     * 매핑 여부 확인
     */
    public boolean isMatched() {
        return "MATCHED".equals(this.mappingStatus);
    }
    
    /**
     * 처리 완료 여부 확인
     */
    public boolean isProcessed() {
        return "PROCESSED".equals(this.processingStatus);
    }
    
    /**
     * 매핑 가능 여부 확인 (운송장번호가 있는 경우)
     */
    public boolean isMappable() {
        return this.trackingNumber != null && !this.trackingNumber.trim().isEmpty();
    }
    
    /**
     * 전체 상품 정보 문자열 반환
     */
    public String getFullProductInfo() {
        StringBuilder sb = new StringBuilder();
        if (productCode != null && !productCode.trim().isEmpty()) {
            sb.append(productCode);
        }
        if (productColor != null && !productColor.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(productColor);
        }
        if (productSize != null && !productSize.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(productSize);
        }
        if (quantity != null && quantity > 0) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(quantity).append("개");
        }
        return sb.toString();
    }
    
    /**
     * 호환성을 위한 getter
     */
    public Long getDirectReturnId() {
        return this.id;
    }
    
    /**
     * 호환성을 위한 setter
     */
    public void setDirectReturnId(Long directReturnId) {
        this.id = directReturnId;
    }
} 