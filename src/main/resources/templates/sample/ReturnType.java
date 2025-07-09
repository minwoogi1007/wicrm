package com.wio.repairsystem.model;

/**
 * 교환/반품 유형 열거형
 * 
 * 사용 화면:
 * - templates/exchange/list.html : 유형별 필터링 및 표시
 * - templates/exchange/form.html : 유형 선택 드롭다운
 * - templates/exchange/view.html : 유형 표시
 * 
 * 테이블: TB_RETURN_TYPE과 연동
 * 참고: 실제 운영에서는 전체반품, 부분반품, 교환으로 세분화됨
 */
public enum ReturnType {
    EXCHANGE("교환"),
    RETURN("반품");

    private final String description;

    ReturnType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 