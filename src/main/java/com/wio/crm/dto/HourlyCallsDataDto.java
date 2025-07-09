package com.wio.crm.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HourlyCallsDataDto {
    private List<String> hours;           // 시간대 라벨 (00시, 01시, ...)
    private List<Integer> totalCalls;     // 시간대별 총콜 수
    private List<Integer> totalCallsYesterday; // 어제 동일 시간대 총콜 수
    
    // 기본 생성자
    public HourlyCallsDataDto() {
    }
    
    // 모든 필드를 포함하는 생성자
    public HourlyCallsDataDto(List<String> hours, List<Integer> totalCalls, List<Integer> totalCallsYesterday) {
        this.hours = hours;
        this.totalCalls = totalCalls;
        this.totalCallsYesterday = totalCallsYesterday;
    }
} 