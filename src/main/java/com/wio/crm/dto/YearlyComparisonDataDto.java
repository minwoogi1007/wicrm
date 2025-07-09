package com.wio.crm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class YearlyComparisonDataDto {
    private long totalCallsCurrentYear;
    private long totalCallsPreviousYear;
    private long cmplCslCurrYr;
    private long cmplCslPrevYr;

    // 증감률 필드 추가
    private double totalCallsComparisonRate;
    private double completedCounselingCallsComparisonRate;

    private Double completionRateComparisonRate;  // 완료율(응답률) 증감률
    private Double answerRateComparisonRate;      // 응대율 증감률
    private Double currentCompletionRate;         // 현재 기간 완료율(응답률)
    private Double previousCompletionRate;        // 이전 기간 완료율(응답률)

    // 기본 생성자
    public YearlyComparisonDataDto() {
    }

    // 모든 필드를 포함하는 생성자 (필요에 따라 추가)
    public YearlyComparisonDataDto(long totalCallsCurrentYear, long totalCallsPreviousYear, long cmplCslCurrYr, long cmplCslPrevYr) {
        this.totalCallsCurrentYear = totalCallsCurrentYear;
        this.totalCallsPreviousYear = totalCallsPreviousYear;
        this.cmplCslCurrYr = cmplCslCurrYr;
        this.cmplCslPrevYr = cmplCslPrevYr;
    }
} 