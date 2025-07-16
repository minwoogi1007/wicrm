package com.wio.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LMS 추적 검색 조건 DTO
 * 
 * @author 개발팀
 * @since 2025.01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LmsTrackingSearchDto {
    
    // 기본 검색 조건
    private String startDate;           // 시작일 (YYYY-MM-DD)
    private String endDate;             // 종료일 (YYYY-MM-DD)
    private String clid;                // 전화번호
    private String status;              // 발송상태 (SENT/PENDING/FAILED/ERROR)
    private String keyword;             // 키워드 (제목, 내용 검색)
    
    // 고급 검색 조건
    private String custCode;            // 고객 코드 (P000000179)
    private Boolean hasFollowUp;        // 후속 연락 여부
    private String callResult;          // 통화 결과
    private String agentName;           // 상담원명
    
    // 페이징 관련
    private int page;                   // 페이지 번호 (1부터 시작)
    private int size;                   // 페이지 크기
    private int offset;                 // 오프셋 (계산됨)
    private int startRow;               // 시작 행 (Oracle용)
    private int endRow;                 // 끝 행 (Oracle용)
    
    // 정렬 관련
    private String sortBy;              // 정렬 필드
    private String sortDir;             // 정렬 방향 (ASC/DESC)
    
    /**
     * 페이징 계산 메소드 (Oracle ROWNUM 기반)
     */
    public void calculatePaging() {
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        
        this.offset = (page - 1) * size;
        this.startRow = offset;  // Oracle의 경우 RN > startRow 이므로
        this.endRow = offset + size;
    }
    
    /**
     * 기본값 설정
     */
    public void setDefaults() {
        if (page == 0) page = 1;
        if (size == 0) size = 20;
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "SEND_DATE";
        }
        if (sortDir == null || sortDir.trim().isEmpty()) {
            sortDir = "DESC";
        }
        if (custCode == null || custCode.trim().isEmpty()) {
            custCode = "P000000179"; // 기본 고객 코드
        }
        
        calculatePaging();
    }
    
    /**
     * 검색 조건 유효성 검사
     */
    public boolean isValid() {
        // 최소한 날짜 범위는 있어야 함
        return startDate != null && !startDate.trim().isEmpty() &&
               endDate != null && !endDate.trim().isEmpty();
    }
    
    /**
     * 키워드 검색 여부
     */
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
    
    /**
     * 전화번호 검색 여부
     */
    public boolean hasPhoneFilter() {
        return clid != null && !clid.trim().isEmpty();
    }
    
    /**
     * 상태 필터 여부
     */
    public boolean hasStatusFilter() {
        return status != null && !status.trim().isEmpty();
    }
    
    /**
     * 후속 연락 필터 여부
     */
    public boolean hasFollowUpFilter() {
        return hasFollowUp != null;
    }
    
    /**
     * 전화번호 정규화 (하이픈 제거 등)
     */
    public String getNormalizedClid() {
        if (clid == null) return null;
        
        // 하이픈, 공백 제거
        return clid.replaceAll("[\\s-]", "");
    }
    
    /**
     * SQL LIKE용 키워드 반환
     */
    public String getLikeKeyword() {
        if (!hasKeyword()) return null;
        
        return "%" + keyword.trim() + "%";
    }
} 