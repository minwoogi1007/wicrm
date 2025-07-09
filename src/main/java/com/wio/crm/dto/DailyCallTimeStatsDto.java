package com.wio.crm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DailyCallTimeStatsDto {
    private String statDate; // 통계 기준일 (YYYYMMDD)
    private long totalCallDurationSeconds; // 총 통화 시간 (초)
    private Double averageCallDurationSeconds; // 평균 통화 시간 (초)

    // 필요시 추가될 수 있는 필드 (CALL_LOG_D에 관련 데이터가 있다면)
    // private long totalTalkDurationSeconds; // 총 순수 통화 시간 (초)
    // private Double averageTalkDurationSeconds; // 평균 순수 통화 시간 (초)

    // 기본 생성자
    public DailyCallTimeStatsDto() {
    }

    // 모든 필드를 포함하는 생성자 (필요에 따라 추가)
    public DailyCallTimeStatsDto(String statDate, long totalCallDurationSeconds, Double averageCallDurationSeconds) {
        this.statDate = statDate;
        this.totalCallDurationSeconds = totalCallDurationSeconds;
        this.averageCallDurationSeconds = averageCallDurationSeconds;
    }
} 