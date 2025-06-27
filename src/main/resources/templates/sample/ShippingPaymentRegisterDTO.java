package com.wio.repairsystem.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 배송비 입금 등록 DTO
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingPaymentRegisterDTO {
    
    private Long registerId;
    private String brand; // RENOMA, CORALIK
    private String siteName;
    private String bankName; // 우리은행, 하나은행
    private String customerName;
    private String customerPhone;
    private Integer amount; // 입금 금액
    private LocalDateTime paymentDate; // 입금일시
    private LocalDateTime registerDate; // 등록일자
    private String registrar; // 등록자
    private String mappingStatus; // PENDING(미매핑), MAPPED(매핑완료)
    private Long returnItemId; // 매핑된 교환/반품 ID
    private String notes; // 비고
    
    // 추가 정보 (조회용)
    private String returnOrderNumber; // 매핑된 교환/반품 주문번호
    private String returnCustomerName; // 매핑된 교환/반품 고객명
    private String returnCustomerPhone; // 매핑된 교환/반품 연락처
    private String mappingStatusText; // 매핑 상태 텍스트
    private String brandText; // 브랜드 텍스트
    
    /**
     * 매핑 상태 텍스트 가져오기
     */
    public String getMappingStatusText() {
        if ("PENDING".equals(mappingStatus)) {
            return "미매핑";
        } else if ("MAPPED".equals(mappingStatus)) {
            return "매핑완료";
        }
        return mappingStatus;
    }
    
    /**
     * 브랜드 텍스트 가져오기
     */
    public String getBrandText() {
        if ("RENOMA".equals(brand)) {
            return "레노마";
        } else if ("CORALIK".equals(brand)) {
            return "코랄리크";
        }
        return brand;
    }
    
    /**
     * 매핑 완료 여부 확인
     */
    public boolean isMapped() {
        return "MAPPED".equals(mappingStatus);
    }
    
    /**
     * 미매핑 여부 확인
     */
    public boolean isPending() {
        return "PENDING".equals(mappingStatus);
    }
    
    /**
     * 브랜드별 기본 은행 가져오기
     */
    public static String getDefaultBankByBrand(String brand) {
        if ("RENOMA".equals(brand)) {
            return "우리은행";
        } else if ("CORALIK".equals(brand)) {
            return "하나은행";
        }
        return "";
    }
} 