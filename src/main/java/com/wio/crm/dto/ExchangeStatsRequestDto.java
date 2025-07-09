package com.wio.crm.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 교환반품 통계 요청 DTO (신규 설계 적용)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeStatsRequestDto {
    
    /**
     * 날짜 기준 타입 ("CS_RECEIVED" 또는 "ORDER_DATE")
     */
    private String dateFilterType;
    
    /**
     * CS접수 시작일 (CS_RECEIVED_DATE 기준)
     */
    private String csStartDate;
    
    /**
     * CS접수 종료일 (CS_RECEIVED_DATE 기준)
     */
    private String csEndDate;
    
    /**
     * 주문 시작일 (ORDER_DATE 기준)
     */
    private String orderStartDate;
    
    /**
     * 주문 종료일 (ORDER_DATE 기준)
     */
    private String orderEndDate;
    
    /**
     * 교환반품 유형 (FULL_EXCHANGE, PARTIAL_EXCHANGE, FULL_RETURN, PARTIAL_RETURN)
     */
    private String returnType;
    
    /**
     * 완료 상태 (COMPLETED, INCOMPLETE)
     */
    private String completionStatus;
    
    /**
     * 사이트명
     */
    private String siteName;
    
    /**
     * 기간 타입 (TODAY, YESTERDAY, WEEK, MONTH, CUSTOM)
     */
    private String periodType;
    

} 