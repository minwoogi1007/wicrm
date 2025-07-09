package com.wio.crm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExchangeSitePerformanceDto {
    private String siteName; // 사이트명
    private Long totalCount; // 총 건수
    private Long completedCount; // 완료 건수
    private Long incompleteCount; // 미완료 건수
    private Double completionRate; // 완료율 (%)
    private Double percentage; // 전체 대비 비율 (%)
    
    // 금액 관련
    private Long totalRefundAmount; // 총 환불금액
    private Double averageRefundAmount; // 평균 환불금액
    private Long totalShippingFee; // 총 배송비
    
    // 처리 시간 관련
    private Double averageProcessingDays; // 평균 처리 기간 (일)
    
    // 유형별 분포
    private Long fullExchangeCount; // 전체교환 건수
    private Long partialExchangeCount; // 부분교환 건수
    private Long fullReturnCount; // 전체반품 건수
    private Long partialReturnCount; // 부분반품 건수
    
    // 기본 생성자
    public ExchangeSitePerformanceDto() {
    }
    
    // 주요 필드 생성자
    public ExchangeSitePerformanceDto(String siteName, Long totalCount, Long completedCount, 
                                      Long incompleteCount, Double completionRate) {
        this.siteName = siteName;
        this.totalCount = totalCount;
        this.completedCount = completedCount;
        this.incompleteCount = incompleteCount;
        this.completionRate = completionRate;
    }
} 