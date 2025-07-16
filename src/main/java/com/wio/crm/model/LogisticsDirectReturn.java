package com.wio.crm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 물류센터 직접 입고 관리 엔티티
 * 고객이 교환반품 신청 없이 물류센터로 직접 보낸 상품 관리
 */
@Entity
@Table(name = "TB_LOGISTICS_DIRECT_RETURN")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogisticsDirectReturn {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "logistics_direct_return_seq")
    @SequenceGenerator(name = "logistics_direct_return_seq", sequenceName = "TB_LOGISTICS_DIRECT_RETURN_SEQ", allocationSize = 1)
    @Column(name = "DIRECT_RETURN_ID")
    private Long id;
    
    @Column(name = "RECEIVED_DATE", nullable = false)
    private LocalDate receivedDate;
    
    @Column(name = "SITE_NAME", length = 100)
    private String siteName;
    
    @Column(name = "CUSTOMER_NAME", length = 100, nullable = false)
    private String customerName;
    
    @Column(name = "CUSTOMER_PHONE", length = 20)
    private String customerPhone;
    
    // 상품 정보
    @Column(name = "PRODUCT_CODE", length = 100)
    private String productCode;
    
    @Column(name = "PRODUCT_COLOR", length = 50)
    private String productColor;
    
    @Column(name = "PRODUCT_SIZE", length = 50)
    private String productSize;
    
    @Column(name = "QUANTITY", nullable = false)
    @Builder.Default
    private Integer quantity = 1;
    
    // 배송 정보
    @Column(name = "TRACKING_NUMBER", length = 100)
    private String trackingNumber;
    
    @Column(name = "COURIER_COMPANY", length = 50)
    private String courierCompany;
    
    // 처리 정보
    @Column(name = "REMARKS", length = 1000)
    private String remarks;
    
    @Column(name = "PROCESSING_STATUS", length = 20, nullable = false)
    @Builder.Default
    private String processingStatus = "RECEIVED";
    
    // 매핑 정보
    @Column(name = "MATCHED_RETURN_ID")
    private Long matchedReturnId;
    
    @Column(name = "MAPPING_STATUS", length = 20, nullable = false)
    @Builder.Default
    private String mappingStatus = "PENDING";
    
    @Column(name = "MAPPING_DATE")
    private LocalDateTime mappingDate;
    
    @Column(name = "MAPPING_BY", length = 50)
    private String mappingBy;
    
    // 시스템 관리
    @Column(name = "CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "UPDATED_DATE", nullable = false)
    private LocalDateTime updatedDate;
    
    @Column(name = "CREATED_BY", length = 50)
    private String createdBy;
    
    @Column(name = "UPDATED_BY", length = 50)
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
    
    // 비즈니스 메서드들
    
    /**
     * 처리 상태 라벨 반환
     */
    public String getProcessingStatusLabel() {
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
     * 매핑 상태 라벨 반환
     */
    public String getMappingStatusLabel() {
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
     * 호환성을 위한 getter (기존 코드와의 호환)
     */
    public Long getDirectReturnId() {
        return this.id;
    }
    
    /**
     * 호환성을 위한 setter (기존 코드와의 호환)
     */
    public void setDirectReturnId(Long directReturnId) {
        this.id = directReturnId;
    }
} 