package com.wio.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 교환/반품 일괄 날짜 업데이트 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnItemBulkDateUpdateDTO {
    private Long id;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate collectionCompletedDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate logisticsConfirmedDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate shippingDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate refundDate;
    
    private String updatedBy;
} 