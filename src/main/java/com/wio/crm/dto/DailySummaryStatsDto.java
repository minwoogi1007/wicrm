package com.wio.crm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DailySummaryStatsDto {
    private String statDate; // 통계 기준일 (YYYYMMDD)
    private long totalCalls; // 총콜
    private long answeredCalls; // 응대호
    private long abandonedCalls; // 포기호
    private long uniqueInboundCalls; // 인입호 (중복제외)
    private long busyCalls; // 부재호
    private long completedCounselingCalls; // 총상담완료호 (TBND01 기준)
    private long inboundCalls; // 수신 콜 수
    private long outboundCalls; // 발신 콜 수

    // 비율 등 계산된 값은 서비스 계층에서 채워질 수 있음
    private Double answerRate; // 응대율
    private Double completionRate; // 완료율
    private Double inboundRate; // 수신 비율
    private Double outboundRate; // 발신 비율

    // 기본 생성자
    public DailySummaryStatsDto() {
    }

    // 모든 필드를 포함하는 생성자 (필요에 따라 추가)
    public DailySummaryStatsDto(String statDate, long totalCalls, long answeredCalls, long abandonedCalls, long uniqueInboundCalls, long busyCalls, long completedCounselingCalls) {
        this.statDate = statDate;
        this.totalCalls = totalCalls;
        this.answeredCalls = answeredCalls;
        this.abandonedCalls = abandonedCalls;
        this.uniqueInboundCalls = uniqueInboundCalls;
        this.busyCalls = busyCalls;
        this.completedCounselingCalls = completedCounselingCalls;
    }
} 