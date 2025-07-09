package com.wio.crm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DailyCounselingTypeStatsDto {
    private String counselingType; // 상담 유형 코드 또는 ID
    private String counselingTypeName; // 상담 유형명
    private long count; // 해당 유형 건수

    // 비율은 서비스 계층에서 계산되어 채워질 수 있음
    private Double percentage; // 해당 유형 비율

    // 기본 생성자
    public DailyCounselingTypeStatsDto() {
    }

    // 모든 필드를 포함하는 생성자 (필요에 따라 추가)
    public DailyCounselingTypeStatsDto(String counselingType, String counselingTypeName, long count) {
        this.counselingType = counselingType;
        this.counselingTypeName = counselingTypeName;
        this.count = count;
    }
} 