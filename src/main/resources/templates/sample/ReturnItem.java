package com.wio.repairsystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 교환/반품 정보 엔티티
 * 
 * 사용 화면:
 * - templates/exchange/list.html : 목록 화면에서 데이터 표시
 * - templates/exchange/form.html : 등록/수정 화면에서 데이터 입력
 * - templates/exchange/view.html : 상세 화면에서 데이터 조회
 * 
 * 테이블: TB_RETURN_ITEM
 */

@Entity
@Table(name = "TB_RETURN_ITEM")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "return_item_seq")
    @SequenceGenerator(name = "return_item_seq", sequenceName = "TB_RETURN_ITEM_SEQ", allocationSize = 1)
    @Column(name = "RETURN_ID")
    private Long id;

    @Column(name = "RETURN_TYPE_CODE", length = 20, nullable = false)
    private String returnTypeCode;

    @Column(name = "ORDER_DATE")
    private LocalDate orderDate;

    @Column(name = "CS_RECEIVED_DATE", nullable = false)
    private LocalDate csReceivedDate;

    @Column(name = "SITE_NAME", length = 50)
    private String siteName;

    @Column(name = "ORDER_NUMBER", length = 50, nullable = false)
    private String orderNumber;

    @Column(name = "REFUND_AMOUNT", precision = 10, scale = 0)
    private Long refundAmount;

    @Column(name = "CUSTOMER_NAME", length = 50, nullable = false)
    private String customerName;

    @Column(name = "CUSTOMER_PHONE", length = 20)
    private String customerPhone;

    @Column(name = "ORDER_ITEM_CODE", length = 50)
    private String orderItemCode;

    @Column(name = "PRODUCT_COLOR", length = 20)
    private String productColor;

    @Column(name = "PRODUCT_SIZE", length = 20)
    private String productSize;

    @Column(name = "QUANTITY", precision = 3, scale = 0)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "SHIPPING_FEE", length = 20)
    private String shippingFee;

    @Column(name = "RETURN_REASON", length = 100)
    private String returnReason;

    @Column(name = "DEFECT_DETAIL", length = 500)
    private String defectDetail;

    @Column(name = "DEFECT_PHOTO_URL", length = 500)
    private String defectPhotoUrl;

    @Column(name = "TRACKING_NUMBER", length = 50)
    private String trackingNumber;

    @Column(name = "COLLECTION_COMPLETED_DATE")
    private LocalDate collectionCompletedDate;

    @Column(name = "LOGISTICS_CONFIRMED_DATE")
    private LocalDate logisticsConfirmedDate;

    @Column(name = "SHIPPING_DATE")
    private LocalDate shippingDate;

    @Column(name = "REFUND_DATE")
    private LocalDate refundDate;

    @Column(name = "IS_COMPLETED", precision = 1, scale = 0)
    @Builder.Default
    private Integer isCompleted = 0;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

    @Column(name = "RETURN_STATUS_CODE", length = 20)
    @Builder.Default
    private String returnStatusCode = "PENDING";

    @Column(name = "PROCESSOR", length = 50)
    private String processor;

    @Column(name = "CREATE_DATE", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createDate;

    @Column(name = "UPDATE_DATE", nullable = false)
    private LocalDateTime updateDate;

    @Column(name = "CREATED_BY", length = 50)
    private String createdBy;

    @Column(name = "UPDATED_BY", length = 50)
    private String updatedBy;

    @Column(name = "PAYMENT_STATUS", length = 20)
    @Builder.Default
    private String paymentStatus = "NOT_REQUIRED";

    @Column(name = "PAYMENT_ID", precision = 10, scale = 0)
    private Long paymentId;

    // 각 날짜별 수정자 정보
    @Column(name = "COLLECTION_UPDATED_BY", length = 50)
    private String collectionUpdatedBy;

    @Column(name = "LOGISTICS_UPDATED_BY", length = 50)
    private String logisticsUpdatedBy;

    @Column(name = "SHIPPING_UPDATED_BY", length = 50)
    private String shippingUpdatedBy;

    @Column(name = "REFUND_UPDATED_BY", length = 50)
    private String refundUpdatedBy;

    // 각 날짜별 수정 시간
    @Column(name = "COLLECTION_UPDATED_DATE")
    private LocalDateTime collectionUpdatedDate;

    @Column(name = "LOGISTICS_UPDATED_DATE")
    private LocalDateTime logisticsUpdatedDate;

    @Column(name = "SHIPPING_UPDATED_DATE")
    private LocalDateTime shippingUpdatedDate;

    @Column(name = "REFUND_UPDATED_DATE")
    private LocalDateTime refundUpdatedDate;

    @PreUpdate
    public void preUpdate() {
        this.updateDate = LocalDateTime.now();
    }

    // 배송비 입금 상태 텍스트 반환
    public String getPaymentStatusText() {
        // 🔥 배송비가 "입금예정"인 경우 우선 처리
        if (shippingFee != null) {
            if (shippingFee.equals("입금예정")) {
                return "입금예정";
            }
        }
        
        // paymentStatus 기준 처리
        if (paymentStatus == null) return "입금불필요";
        
        switch (paymentStatus) {
            case "PENDING": return "입금예정";
            case "COMPLETED": return "입금완료";
            case "NOT_REQUIRED": return "입금불필요";
            default: return paymentStatus;
        }
    }

    // 교환 출고 가능 여부 확인
    public boolean canShipExchange() {
        // 교환인 경우만 체크
        if (returnTypeCode == null || 
            !(returnTypeCode.contains("EXCHANGE") || returnTypeCode.equals("EXCHANGE"))) {
            return false;
        }
        
        // 🔥 배송비가 "입금예정"인 경우 입금 완료 확인 필요
        if (shippingFee != null && shippingFee.equals("입금예정")) {
            return paymentStatus != null && paymentStatus.equals("COMPLETED");
        }
        
        // 나머지 경우(무상 등)는 출고 가능
        return paymentStatus == null || 
               paymentStatus.equals("COMPLETED") || 
               paymentStatus.equals("NOT_REQUIRED");
    }
}
