package com.wio.repairsystem.model;

/**
 * 교환/반품 상태 열거형
 * 
 * 사용 화면:
 * - templates/exchange/list.html : 상태별 필터링 및 표시
 * - templates/exchange/form.html : 상태 선택 드롭다운
 * - templates/exchange/view.html : 현재 상태 표시
 * 
 * 테이블: TB_RETURN_STATUS와 연동
 */
public enum ReturnStatus {
    PENDING("접수대기"),
    PROCESSING("처리중"),
    SHIPPING("배송중"),
    COMPLETED("완료"),
    CANCELED("취소");

    private final String description;

    ReturnStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}