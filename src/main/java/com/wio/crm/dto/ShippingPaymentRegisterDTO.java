package com.wio.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 배송비 입금 등록 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingPaymentRegisterDTO {
    private Long registerId;
    private String brand;
    private String siteName;
    private String bankName;
    private String customerName;
    private String customerPhone;
    private Long amount;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime paymentDate;
    
    private String notes;
    private String registrar;
    private LocalDateTime registerDate;
    private String mappingStatus;
    private Long returnItemId;
    private LocalDateTime mappedDate;
    
    /**
     * 브랜드별 기본 은행 반환
     */
    public static String getDefaultBankByBrand(String brand) {
        if (brand == null) return "우리은행";
        
        switch (brand.toUpperCase()) {
            case "RENOMA":
                return "우리은행";
            case "CORALIK":
                return "하나은행";
            default:
                return "우리은행";
        }
    }
    

} 