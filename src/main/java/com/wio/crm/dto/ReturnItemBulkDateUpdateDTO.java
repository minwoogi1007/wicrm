package com.wio.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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
    
    private String remarks;  // 비고 (새로 추가)
    
    private String updatedBy;
    
    // 변경된 필드들을 추적하기 위한 Set
    @Builder.Default
    private Set<String> changedFields = new HashSet<>();
    
    /**
     * 필드가 변경되었는지 확인
     */
    public boolean hasFieldChanged(String fieldName) {
        return changedFields.contains(fieldName);
    }
    
    /**
     * 변경된 필드 추가
     */
    public void addChangedField(String fieldName) {
        changedFields.add(fieldName);
    }
} 