package com.wio.crm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeeklyCallTimeStatsDto {
    private String statDate; // 통계 기준일
    private long totalCallDurationSeconds;
    private Double averageCallDurationSeconds;

    // 기본 생성자
    public WeeklyCallTimeStatsDto() {
    }

    // 모든 필드를 포함하는 생성자 (필요에 따라 추가)
    public WeeklyCallTimeStatsDto(String statDate, long totalCallDurationSeconds, Double averageCallDurationSeconds) {
        this.statDate = statDate;
        this.totalCallDurationSeconds = totalCallDurationSeconds;
        this.averageCallDurationSeconds = averageCallDurationSeconds;
    }
} 