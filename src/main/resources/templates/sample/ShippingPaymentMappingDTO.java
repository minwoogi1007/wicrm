package com.wio.repairsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 배송비 입금 매핑 DTO
 * 상담원의 매핑 작업을 위한 통합 데이터 구조
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingPaymentMappingDTO {
    
    /**
     * 입금 내역 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentInfo {
        private Long paymentId;
        private String customerName;
        private String customerPhone;
        private Integer amount;
        private LocalDateTime paymentDate;
        private String bankName;
        private String brand;
        private String siteName;
        private String mappingStatus; // PENDING, MAPPED, CANCELLED
        private String notes;
        private LocalDateTime registerDate;
        private String registeredBy;
    }
    
    /**
     * 교환/반품 후보 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReturnItemCandidate {
        private Long returnItemId;
        private String orderNumber;
        private String orderItemCode;
        private String customerName;
        private String customerPhone;
        private String siteName;
        private Integer shippingFee;
        private String returnTypeCode;
        private String returnReason;
        private LocalDate csReceivedDate;
        private String paymentStatus;
        
        // 매칭 점수 관련
        private Integer matchScore; // 0-100점
        private String matchLevel; // HIGH(90+), MEDIUM(70-89), LOW(50-69)
        private String matchReason; // 매칭 근거 설명
        
        // 추가 정보
        private String productColor;
        private String productSize;
        private Integer quantity;
        private String trackingNumber;
        private Boolean canShipExchange; // 교환 출고 가능 여부
    }
    
    /**
     * 매핑 요청 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MappingRequest {
        private Long paymentId;
        private Long returnItemId;
        private String notes;
        private String mappedBy;
    }
    
    /**
     * 매핑 결과 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MappingResult {
        private Long mappingId;
        private PaymentInfo payment;
        private ReturnItemCandidate returnItem;
        private LocalDateTime mappedDate;
        private String mappedBy;
        private String notes;
        private Boolean canShipExchange;
        private String resultMessage;
    }
    
    /**
     * 대시보드 통계 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardStats {
        private Long totalPayments;
        private Long mappedPayments;
        private Long pendingPayments;
        private Long totalAmount;
        private Long mappedAmount;
        private Long pendingAmount;
        
        // 브랜드별 통계
        private Long renomaCount;
        private Long coralikCount;
        private Long renomaAmount;
        private Long coralikAmount;
        
        // 오늘 통계
        private Long todayRegistered;
        private Long todayMapped;
    }
    
    /**
     * 통합 매핑 화면용 데이터
     */
    private PaymentInfo payment;
    private List<ReturnItemCandidate> candidates;
    private DashboardStats stats;
    private Integer totalCandidates;
    private String searchKeyword;
} 