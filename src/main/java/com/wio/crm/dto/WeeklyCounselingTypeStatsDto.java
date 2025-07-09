package com.wio.crm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeeklyCounselingTypeStatsDto {
    private String counselingType;
    private String counselingTypeName;
    private long count;
    private Double percentage;

    // 기본 생성자
    public WeeklyCounselingTypeStatsDto() {
    }

    // 모든 필드를 포함하는 생성자 (필요에 따라 추가)
    public WeeklyCounselingTypeStatsDto(String counselingType, String counselingTypeName, long count, Double percentage) {
        this.counselingType = counselingType;
        this.counselingTypeName = counselingTypeName;
        this.count = count;
        this.percentage = percentage;
    }
} 