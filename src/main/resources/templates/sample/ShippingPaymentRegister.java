package com.wio.repairsystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 배송비 입금 등록 정보 엔티티
 * 
 * 사용 화면:
 * - templates/shipping-payment/register.html : 입금 등록 화면
 * - templates/shipping-payment/list.html : 입금 관리 목록 화면
 * 
 * 테이블: TB_SHIPPING_PAYMENT_REGISTER
 */
@Entity
@Table(name = "TB_SHIPPING_PAYMENT_REGISTER")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingPaymentRegister {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shipping_payment_seq")
    @SequenceGenerator(name = "shipping_payment_seq", sequenceName = "SEQ_SHIPPING_PAYMENT_REGISTER", allocationSize = 1)
    @Column(name = "REGISTER_ID")
    private Long registerId;
    
    @Column(name = "BRAND", length = 20, nullable = false)
    private String brand; // RENOMA, CORALIK
    
    @Column(name = "SITE_NAME", length = 100, nullable = false)
    private String siteName;
    
    @Column(name = "BANK_NAME", length = 50, nullable = false)
    private String bankName; // 우리은행, 하나은행
    
    @Column(name = "CUSTOMER_NAME", length = 100, nullable = false)
    private String customerName;
    
    @Column(name = "CUSTOMER_PHONE", length = 20, nullable = false)
    private String customerPhone;
    
    @Column(name = "AMOUNT", nullable = false)
    private Integer amount; // 입금 금액
    
    @Column(name = "PAYMENT_DATE", nullable = false)
    private LocalDateTime paymentDate; // 입금일시
    
    @Column(name = "REGISTER_DATE", nullable = false)
    private LocalDateTime registerDate; // 등록일자
    
    @Column(name = "REGISTRAR", length = 50, nullable = false)
    private String registrar; // 등록자
    
    @Column(name = "MAPPING_STATUS", length = 20, nullable = false)
    private String mappingStatus; // PENDING(미매핑), MAPPED(매핑완료)
    
    @Column(name = "RETURN_ITEM_ID")
    private Long returnItemId; // 매핑된 교환/반품 ID
    
    @Column(name = "NOTES", length = 1000)
    private String notes; // 비고
    
    @Column(name = "MAPPED_DATE")
    private LocalDateTime mappedDate; // 매핑 완료일시
    
    @PrePersist
    protected void onCreate() {
        registerDate = LocalDateTime.now();
        if (mappingStatus == null) {
            mappingStatus = "PENDING";
        }
    }
    
    /**
     * 브랜드별 기본 은행 정보 가져오기
     */
    public static String getDefaultBankByBrand(String brand) {
        if ("RENOMA".equals(brand)) {
            return "우리은행";
        } else if ("CORALIK".equals(brand)) {
            return "하나은행";
        }
        return "";
    }
    
    /**
     * 매핑 상태가 완료인지 확인
     */
    public boolean isMapped() {
        return "MAPPED".equals(mappingStatus);
    }
    
    /**
     * 매핑 상태가 대기중인지 확인
     */
    public boolean isPending() {
        return "PENDING".equals(mappingStatus);
    }
} 