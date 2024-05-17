package com.wio.crm.model;

import org.apache.ibatis.annotations.Mapper;


public class Comment {

    private String callCode;
    private String personCode;
    private String projectCode;
    private String custCode;

    private String serlNo;
    private String userId;
    private String conText;
    private String saupGubn;
    private String delGubn;
    private String confirmYn;

    private String inDate;

    public String getInDate() {
        return inDate;
    }

    public void setInDate(String inDate) {
        this.inDate = inDate;
    }

    public String getCallCode() {
        return callCode;
    }

    public void setCallCode(String callCode) {
        this.callCode = callCode;
    }

    public String getPersonCode() {
        return personCode;
    }

    public void setPersonCode(String personCode) {
        this.personCode = personCode;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getCustCode() {
        return custCode;
    }

    public void setCustCode(String custCode) {
        this.custCode = custCode;
    }

    public String getSerlNo() {
        return serlNo;
    }

    public void setSerlNo(String serlNo) {
        this.serlNo = serlNo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getConText() {
        return conText;
    }

    public void setConText(String conText) {
        this.conText = conText;
    }

    public String getSaupGubn() {
        return saupGubn;
    }

    public void setSaupGubn(String saupGubn) {
        this.saupGubn = saupGubn;
    }

    public String getDelGubn() {
        return delGubn;
    }

    public void setDelGubn(String delGubn) {
        this.delGubn = delGubn;
    }

    public String getConfirmYn() {
        return confirmYn;
    }

    public void setConfirmYn(String confirmYn) {
        this.confirmYn = confirmYn;
    }
}
