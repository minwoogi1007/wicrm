package com.wio.repairsystem.dto;

import java.time.LocalDate;

/**
 * 교환/반품 일괄 날짜 업데이트 DTO
 * 대표님 요청: 리스트에서 직접 날짜 수정 기능
 */
public class ReturnItemBulkDateUpdateDTO {
    
    private Long id;
    private LocalDate collectionCompletedDate;    // 회수완료
    private LocalDate logisticsConfirmedDate;     // 물류확인
    private LocalDate shippingDate;               // 출고일자
    private LocalDate refundDate;                 // 환불일자
    private String updatedBy;                     // 수정자
    
    // 기본 생성자
    public ReturnItemBulkDateUpdateDTO() {}
    
    // 전체 생성자
    public ReturnItemBulkDateUpdateDTO(Long id, LocalDate collectionCompletedDate, 
                                      LocalDate logisticsConfirmedDate, LocalDate shippingDate, 
                                      LocalDate refundDate, String updatedBy) {
        this.id = id;
        this.collectionCompletedDate = collectionCompletedDate;
        this.logisticsConfirmedDate = logisticsConfirmedDate;
        this.shippingDate = shippingDate;
        this.refundDate = refundDate;
        this.updatedBy = updatedBy;
    }
    
    // Getter/Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDate getCollectionCompletedDate() {
        return collectionCompletedDate;
    }
    
    public void setCollectionCompletedDate(LocalDate collectionCompletedDate) {
        this.collectionCompletedDate = collectionCompletedDate;
    }
    
    public LocalDate getLogisticsConfirmedDate() {
        return logisticsConfirmedDate;
    }
    
    public void setLogisticsConfirmedDate(LocalDate logisticsConfirmedDate) {
        this.logisticsConfirmedDate = logisticsConfirmedDate;
    }
    
    public LocalDate getShippingDate() {
        return shippingDate;
    }
    
    public void setShippingDate(LocalDate shippingDate) {
        this.shippingDate = shippingDate;
    }
    
    public LocalDate getRefundDate() {
        return refundDate;
    }
    
    public void setRefundDate(LocalDate refundDate) {
        this.refundDate = refundDate;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    @Override
    public String toString() {
        return "ReturnItemBulkDateUpdateDTO{" +
                "id=" + id +
                ", collectionCompletedDate=" + collectionCompletedDate +
                ", logisticsConfirmedDate=" + logisticsConfirmedDate +
                ", shippingDate=" + shippingDate +
                ", refundDate=" + refundDate +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
} 