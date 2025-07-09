package com.wio.crm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonthlyCounselingTypeStatsDto {
    private String counselingType;
    private String counselingTypeName;
    private long count;
    private Double percentage;
    private Double completionRate;

    // 기본 생성자
    public MonthlyCounselingTypeStatsDto() {
    }

    // 모든 필드를 포함하는 생성자 (필요에 따라 추가)
    public MonthlyCounselingTypeStatsDto(String counselingType, String counselingTypeName, long count, Double percentage, Double completionRate) {
        this.counselingType = counselingType;
        this.counselingTypeName = counselingTypeName;
        this.count = count;
        this.percentage = percentage;
        this.completionRate = completionRate;
    }
} 