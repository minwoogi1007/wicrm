package com.wio.crm.model;

import java.util.Date;

/**
 * AI 리포트 서비스 구독 모델
 */
public class AiReportSubscription {
    private Long subscriptionId;
    private String custCode;
    private String planType;        // BASIC, PREMIUM, ENTERPRISE
    private String status;          // ACTIVE, EXPIRED, CANCELLED, PENDING
    private Integer monthlyFee;
    private Date startDate;
    private Date endDate;
    private String autoRenewal;
    private String paymentMethod;
    private String contactEmail;
    private String contactName;
    private String contactPhone;
    private Date inDate;
    private String inEmpno;
    private Date upDate;
    private String upEmpno;
    private String memo;
    
    // 조인 필드
    private String custName;        // 업체명

    // 기본 생성자
    public AiReportSubscription() {
        this.planType = "BASIC";
        this.status = "PENDING";
        this.monthlyFee = 50000;
        this.autoRenewal = "Y";
    }

    // Getter & Setter
    public Long getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getCustCode() { return custCode; }
    public void setCustCode(String custCode) { this.custCode = custCode; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getMonthlyFee() { return monthlyFee; }
    public void setMonthlyFee(Integer monthlyFee) { this.monthlyFee = monthlyFee; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public String getAutoRenewal() { return autoRenewal; }
    public void setAutoRenewal(String autoRenewal) { this.autoRenewal = autoRenewal; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public Date getInDate() { return inDate; }
    public void setInDate(Date inDate) { this.inDate = inDate; }

    public String getInEmpno() { return inEmpno; }
    public void setInEmpno(String inEmpno) { this.inEmpno = inEmpno; }

    public Date getUpDate() { return upDate; }
    public void setUpDate(Date upDate) { this.upDate = upDate; }

    public String getUpEmpno() { return upEmpno; }
    public void setUpEmpno(String upEmpno) { this.upEmpno = upEmpno; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public String getCustName() { return custName; }
    public void setCustName(String custName) { this.custName = custName; }
    
    // 유틸리티 메서드
    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }
    
    public String getPlanTypeName() {
        switch (planType) {
            case "BASIC": return "베이직";
            case "PREMIUM": return "프리미엄";
            case "ENTERPRISE": return "엔터프라이즈";
            default: return planType;
        }
    }
    
    public String getStatusName() {
        switch (status) {
            case "ACTIVE": return "활성";
            case "EXPIRED": return "만료";
            case "CANCELLED": return "취소";
            case "PENDING": return "대기";
            default: return status;
        }
    }
}

