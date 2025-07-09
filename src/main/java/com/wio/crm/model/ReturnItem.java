package com.wio.crm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 교환/반품 엔티티
 */
@Entity
@Table(name = "TB_RETURN_ITEM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "return_item_seq")
    @SequenceGenerator(name = "return_item_seq", sequenceName = "TB_RETURN_ITEM_SEQ", allocationSize = 1)
    @Column(name = "RETURN_ID")
    private Long id;
    
    @Column(name = "RETURN_TYPE_CODE", length = 50)
    private String returnTypeCode;
    
    @Column(name = "ORDER_DATE")
    private LocalDate orderDate;
    
    @Column(name = "CS_RECEIVED_DATE")
    private LocalDate csReceivedDate;
    
    @Column(name = "SITE_NAME", length = 100)
    private String siteName;
    
    @Column(name = "ORDER_NUMBER", length = 100)
    private String orderNumber;
    
    @Column(name = "REFUND_AMOUNT")
    private Long refundAmount;
    
    @Column(name = "CUSTOMER_NAME", length = 100)
    private String customerName;
    
    @Column(name = "CUSTOMER_PHONE", length = 20)
    private String customerPhone;
    
    @Column(name = "ORDER_ITEM_CODE", length = 100)
    private String orderItemCode;
    
    @Column(name = "PRODUCT_COLOR", length = 50)
    private String productColor;
    
    @Column(name = "PRODUCT_SIZE", length = 50)
    private String productSize;
    
    @Column(name = "QUANTITY")
    private Integer quantity;
    
    @Column(name = "SHIPPING_FEE", length = 50)
    private String shippingFee;
    
    @Column(name = "RETURN_REASON", length = 500)
    private String returnReason;
    
    @Column(name = "DEFECT_DETAIL", length = 1000)
    private String defectDetail;
    
    @Column(name = "DEFECT_PHOTO_URL", length = 500)
    private String defectPhotoUrl;
    
    @Column(name = "TRACKING_NUMBER", length = 100)
    private String trackingNumber;
    
    @Column(name = "COLLECTION_COMPLETED_DATE")
    private LocalDate collectionCompletedDate;
    
    @Column(name = "LOGISTICS_CONFIRMED_DATE")
    private LocalDate logisticsConfirmedDate;
    
    @Column(name = "SHIPPING_DATE")
    private LocalDate shippingDate;
    
    @Column(name = "REFUND_DATE")
    private LocalDate refundDate;
    
    @Column(name = "IS_COMPLETED")
    private Boolean isCompleted;
    
    @Column(name = "REMARKS", length = 1000)
    private String remarks;
    
    @Column(name = "RETURN_STATUS_CODE", length = 50)
    private String returnStatusCode;
    
    @Column(name = "PROCESSOR", length = 100)
    private String processor;
    
    @Column(name = "CREATE_DATE")
    private LocalDateTime createDate;
    
    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;
    
    @Column(name = "CREATED_BY", length = 100)
    private String createdBy;
    
    @Column(name = "UPDATED_BY", length = 100)
    private String updatedBy;
    
    @Column(name = "PAYMENT_STATUS", length = 50)
    private String paymentStatus;
    
    @Column(name = "PAYMENT_ID")
    private Long paymentId;
    
    // 수정자 추적 필드들
    @Column(name = "COLLECTION_UPDATED_BY", length = 100)
    private String collectionUpdatedBy;
    
    @Column(name = "COLLECTION_UPDATED_DATE")
    private LocalDateTime collectionUpdatedDate;
    
    @Column(name = "LOGISTICS_UPDATED_BY", length = 100)
    private String logisticsUpdatedBy;
    
    @Column(name = "LOGISTICS_UPDATED_DATE")
    private LocalDateTime logisticsUpdatedDate;
    
    @Column(name = "SHIPPING_UPDATED_BY", length = 100)
    private String shippingUpdatedBy;
    
    @Column(name = "SHIPPING_UPDATED_DATE")
    private LocalDateTime shippingUpdatedDate;
    
    @Column(name = "REFUND_UPDATED_BY", length = 100)
    private String refundUpdatedBy;
    
    @Column(name = "REFUND_UPDATED_DATE")
    private LocalDateTime refundUpdatedDate;
    
    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
        updateDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalDateTime.now();
    }
    
    // 비즈니스 메서드들 (Lombok이 생성하지 않는 커스텀 메서드들)
    
    /**
     * BigDecimal로 환불금액 반환 (호환성 유지)
     */
    public BigDecimal getRefundAmountAsBigDecimal() {
        return refundAmount != null ? BigDecimal.valueOf(refundAmount) : BigDecimal.ZERO;
    }
    
    /**
     * BigDecimal로 환불금액 설정 (호환성 유지)
     */
    public void setRefundAmountFromBigDecimal(BigDecimal refundAmount) {
        this.refundAmount = refundAmount != null ? refundAmount.longValue() : null;
    }
    
    /**
     * 기존 getReturnId 메서드 (호환성 유지)
     */
    public Long getReturnId() {
        return this.id;
    }
    
    /**
     * 기존 setReturnId 메서드 (호환성 유지)
     */
    public void setReturnId(Long returnId) {
        this.id = returnId;
    }

    // 편의 메서드들
    public String getReturnTypeLabel() {
        if (this.returnTypeCode == null) {
            return "";
        }
        switch (this.returnTypeCode) {
            case "EXCHANGE": return "교환";
            case "RETURN": return "반품";
            case "FULL_RETURN": return "전체반품";
            case "PARTIAL_RETURN": return "부분반품";
            default: return this.returnTypeCode;
        }
    }

    public String getReturnStatusLabel() {
        if (this.returnStatusCode == null) {
            return "";
        }
        switch (this.returnStatusCode) {
            case "PENDING": return "접수대기";
            case "PROCESSING": return "처리중";
            case "SHIPPING": return "배송중";
            case "COMPLETED": return "완료";
            case "CANCELED": return "취소";
            default: return this.returnStatusCode;
        }
    }

    public String getPaymentStatusLabel() {
        if (this.paymentStatus == null) {
            return "";
        }
        switch (this.paymentStatus) {
            case "PENDING": return "입금대기";
            case "COMPLETED": return "입금완료";
            case "NOT_REQUIRED": return "입금불필요";
            default: return this.paymentStatus;
        }
    }

    public boolean isCompleted() {
        return this.isCompleted != null && this.isCompleted;
    }
} 