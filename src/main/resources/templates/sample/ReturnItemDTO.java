package com.wio.repairsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 교환/반품 정보 DTO
 * 실제 TB_RETURN_ITEM 테이블 구조와 일치
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnItemDTO {
    private Long id;
    private String returnTypeCode;
    private LocalDate orderDate;
    private LocalDate csReceivedDate;
    private String siteName;
    private String orderNumber;
    private Long refundAmount;
    private String customerName;
    private String customerPhone;
    private String orderItemCode;
    private String productColor;
    private String productSize;
    private Integer quantity;
    private String shippingFee;
    private String returnReason;
    private String defectDetail;
    private String defectPhotoUrl;
    private String trackingNumber;
    private LocalDate collectionCompletedDate;
    private LocalDate logisticsConfirmedDate;
    private LocalDate shippingDate;
    private LocalDate refundDate;
    private Integer isCompleted;
    private String remarks;
    private String returnStatusCode;
    private String processor;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private String createdBy;
    private String updatedBy;
    private String paymentStatus;
    private Long paymentId;
    
    // 🔥 대표님 요청: 각 날짜별 수정자 추적 필드들
    private String collectionUpdatedBy;
    private LocalDateTime collectionUpdatedDate;
    private String logisticsUpdatedBy;
    private LocalDateTime logisticsUpdatedDate;
    private String shippingUpdatedBy;
    private LocalDateTime shippingUpdatedDate;
    private String refundUpdatedBy;
    private LocalDateTime refundUpdatedDate;

    // 배송비 입금 상태 텍스트 반환
    public String getPaymentStatusText() {
        // 🔥 배송비가 "입금필요" 또는 "입금대기"인 경우 우선 처리
        if (shippingFee != null) {
            if (shippingFee.equals("입금필요") || shippingFee.equals("입금대기")) {
                return "입금필요";
            }
        }
        
        // paymentStatus 기준 처리
        if (paymentStatus == null) return "입금불필요";
        
        switch (paymentStatus) {
            case "PENDING": return "입금필요";
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
        
        // 🔥 배송비가 "입금필요" 또는 "입금대기"인 경우 입금 완료 확인 필요
        if (shippingFee != null && (shippingFee.equals("입금필요") || shippingFee.equals("입금대기"))) {
            return paymentStatus != null && paymentStatus.equals("COMPLETED");
        }
        
        // 나머지 경우(무상 등)는 출고 가능
        return paymentStatus == null || 
               paymentStatus.equals("COMPLETED") || 
               paymentStatus.equals("NOT_REQUIRED");
    }
}