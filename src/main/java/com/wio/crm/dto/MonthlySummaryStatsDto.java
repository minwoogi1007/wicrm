package com.wio.crm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonthlySummaryStatsDto {
    private String statDate; // 통계 기준일 (월간의 경우, 해당 월의 시작일 또는 특정 대표일)
    private long totalCalls;
    private long answeredCalls;
    private long abandonedCalls;
    private long uniqueInboundCalls;
    private long busyCalls;
    private long completedCounselingCalls;
    private long inboundCalls; // 수신 콜 수
    private long outboundCalls; // 발신 콜 수
    private Double answerRate; // 응대율
    private Double completionRate; // 완료율
    private Double inboundRate; // 수신 비율
    private Double outboundRate; // 발신 비율

    // 기본 생성자
    public MonthlySummaryStatsDto() {
    }

    // 모든 필드를 포함하는 생성자 (필요에 따라 추가)
    public MonthlySummaryStatsDto(String statDate, long totalCalls, long answeredCalls, long abandonedCalls, long uniqueInboundCalls, long busyCalls, long completedCounselingCalls, Double answerRate, Double completionRate) {
        this.statDate = statDate;
        this.totalCalls = totalCalls;
        this.answeredCalls = answeredCalls;
        this.abandonedCalls = abandonedCalls;
        this.uniqueInboundCalls = uniqueInboundCalls;
        this.busyCalls = busyCalls;
        this.completedCounselingCalls = completedCounselingCalls;
        this.answerRate = answerRate;
        this.completionRate = completionRate;
    }
} 