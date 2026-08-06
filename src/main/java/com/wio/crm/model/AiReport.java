package com.wio.crm.model;

import java.util.Date;

/**
 * AI 분석 리포트 모델
 */
public class AiReport {
    private Long reportId;
    private Long subscriptionId;
    private String custCode;
    private String reportMonth;         // YYYYMM
    private String reportType;          // MONTHLY, WEEKLY, CUSTOM
    private String reportTitle;
    
    // 기본 통계
    private Integer totalConsultations;
    private Integer completedCount;
    private Integer urgentCount;
    private Integer claimCount;
    private Double avgProcessTime;
    
    // AI 분석 결과 (JSON)
    private String summaryJson;
    private String keywordsJson;
    private String sentimentJson;
    private String productJson;
    private String trendJson;
    private String recommendations;
    
    // 파일 정보
    private String pdfPath;
    private String excelPath;
    
    // 처리 정보
    private String status;              // PENDING, PROCESSING, COMPLETED, FAILED
    private String errorMessage;
    private Integer tokenUsed;
    private Integer processingTime;
    
    // 발송 정보
    private String emailSent;
    private Date emailSentDate;
    
    // 감사 정보
    private Date inDate;
    private Date completedDate;
    
    // 조인 필드
    private String custName;
    private String planType;

    // 기본 생성자
    public AiReport() {
        this.reportType = "MONTHLY";
        this.status = "PENDING";
        this.totalConsultations = 0;
        this.completedCount = 0;
        this.urgentCount = 0;
        this.claimCount = 0;
        this.avgProcessTime = 0.0;
        this.tokenUsed = 0;
        this.processingTime = 0;
        this.emailSent = "N";
    }

    // Getter & Setter
    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public Long getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getCustCode() { return custCode; }
    public void setCustCode(String custCode) { this.custCode = custCode; }

    public String getReportMonth() { return reportMonth; }
    public void setReportMonth(String reportMonth) { this.reportMonth = reportMonth; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

    public Integer getTotalConsultations() { return totalConsultations; }
    public void setTotalConsultations(Integer totalConsultations) { this.totalConsultations = totalConsultations; }

    public Integer getCompletedCount() { return completedCount; }
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }

    public Integer getUrgentCount() { return urgentCount; }
    public void setUrgentCount(Integer urgentCount) { this.urgentCount = urgentCount; }

    public Integer getClaimCount() { return claimCount; }
    public void setClaimCount(Integer claimCount) { this.claimCount = claimCount; }

    public Double getAvgProcessTime() { return avgProcessTime; }
    public void setAvgProcessTime(Double avgProcessTime) { this.avgProcessTime = avgProcessTime; }

    public String getSummaryJson() { return summaryJson; }
    public void setSummaryJson(String summaryJson) { this.summaryJson = summaryJson; }

    public String getKeywordsJson() { return keywordsJson; }
    public void setKeywordsJson(String keywordsJson) { this.keywordsJson = keywordsJson; }

    public String getSentimentJson() { return sentimentJson; }
    public void setSentimentJson(String sentimentJson) { this.sentimentJson = sentimentJson; }

    public String getProductJson() { return productJson; }
    public void setProductJson(String productJson) { this.productJson = productJson; }

    public String getTrendJson() { return trendJson; }
    public void setTrendJson(String trendJson) { this.trendJson = trendJson; }

    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }

    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }

    public String getExcelPath() { return excelPath; }
    public void setExcelPath(String excelPath) { this.excelPath = excelPath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Integer getTokenUsed() { return tokenUsed; }
    public void setTokenUsed(Integer tokenUsed) { this.tokenUsed = tokenUsed; }

    public Integer getProcessingTime() { return processingTime; }
    public void setProcessingTime(Integer processingTime) { this.processingTime = processingTime; }

    public String getEmailSent() { return emailSent; }
    public void setEmailSent(String emailSent) { this.emailSent = emailSent; }

    public Date getEmailSentDate() { return emailSentDate; }
    public void setEmailSentDate(Date emailSentDate) { this.emailSentDate = emailSentDate; }

    public Date getInDate() { return inDate; }
    public void setInDate(Date inDate) { this.inDate = inDate; }

    public Date getCompletedDate() { return completedDate; }
    public void setCompletedDate(Date completedDate) { this.completedDate = completedDate; }

    public String getCustName() { return custName; }
    public void setCustName(String custName) { this.custName = custName; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }
    
    // 유틸리티 메서드
    public String getStatusName() {
        switch (status) {
            case "PENDING": return "대기중";
            case "PROCESSING": return "처리중";
            case "COMPLETED": return "완료";
            case "FAILED": return "실패";
            default: return status;
        }
    }
    
    public String getStatusBadgeClass() {
        switch (status) {
            case "PENDING": return "badge-warning";
            case "PROCESSING": return "badge-info";
            case "COMPLETED": return "badge-success";
            case "FAILED": return "badge-danger";
            default: return "badge-secondary";
        }
    }
    
    public double getCompletionRate() {
        if (totalConsultations == 0) return 0;
        return (completedCount * 100.0) / totalConsultations;
    }
    
    public String getFormattedMonth() {
        if (reportMonth == null || reportMonth.length() != 6) return reportMonth;
        return reportMonth.substring(0, 4) + "년 " + reportMonth.substring(4) + "월";
    }
}

