package com.wio.crm.dto;

/**
 * 검색 조건 DTO
 */
public class SearchDTO {
    private String keyword;
    private String startDate;
    private String endDate;

    public SearchDTO() {
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
} 