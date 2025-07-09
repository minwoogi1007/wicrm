package com.wio.repairsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 교환/반품 검색 조건 DTO - 단일 통합검색 방식
 * 
 * 사용 화면:
 * - templates/exchange/list.html : 간단한 통합검색 필터
 * 
 * 검색 조건: 통합검색어 + 날짜범위만으로 단순화
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnItemSearchDTO {
    
    // 통합 검색어 - 모든 텍스트 필드에서 검색
    private String keyword;
    
    // 날짜 범위 - CS접수일 기준
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    // 페이징 및 정렬
    private int page = 0;
    private int size = 20;
    private String sortBy = "id";
    private String sortDir = "DESC";
    
    // Setter 메서드들
    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }
    
    public String getSortDir() {
        return this.sortDir;
    }

    /**
     * 검색 조건이 있는지 확인하는 메서드
     * @return 검색 조건이 하나라도 있으면 true
     */
    public boolean hasSearchCondition() {
        return (keyword != null && !keyword.trim().isEmpty()) ||
               (startDate != null) ||
               (endDate != null);
    }
} 