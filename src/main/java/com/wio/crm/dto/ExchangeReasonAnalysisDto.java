package com.wio.crm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExchangeReasonAnalysisDto {
    private String returnReason; // 반품사유
    private Long count; // 건수
    private Double percentage; // 비율 (%)
    private Long totalRefundAmount; // 해당 사유 총 환불금액
    private Double averageRefundAmount; // 해당 사유 평균 환불금액
    private Double averageProcessingDays; // 해당 사유 평균 처리 기간
    
    // 기본 생성자
    public ExchangeReasonAnalysisDto() {
    }
    
    // 주요 필드 생성자
    public ExchangeReasonAnalysisDto(String returnReason, Long count, Double percentage) {
        this.returnReason = returnReason;
        this.count = count;
        this.percentage = percentage;
    }
    
    // 전체 필드 생성자
    public ExchangeReasonAnalysisDto(String returnReason, Long count, Double percentage, 
                                     Long totalRefundAmount, Double averageRefundAmount, 
                                     Double averageProcessingDays) {
        this.returnReason = returnReason;
        this.count = count;
        this.percentage = percentage;
        this.totalRefundAmount = totalRefundAmount;
        this.averageRefundAmount = averageRefundAmount;
        this.averageProcessingDays = averageProcessingDays;
    }
} 