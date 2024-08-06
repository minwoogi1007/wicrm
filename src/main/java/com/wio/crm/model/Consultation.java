package com.wio.crm.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Consultation {
    private String custCode;
    private String projectCode;
    private String personCode;
    private String callCode;
    private String empNo;
    private String incallNo;
    private String outcallNo;
    private String ringTime;
    private String ringId;
    private Date channelTime;
    private String channelId;
    private String outTime;
    private String outId;
    private String csType;
    private String csNote;
    private String csDate;
    private String csBank;
    private String csName;
    private String csCash;
    private String prcGubn;
    private String prcNote;
    private String emgGubn;
    private String recallEmp;
    private String recallCust;
    private String saupGubn;
    private String delGubn;
    private String inDate;
    private String inEmpno;
    private String up_Date;
    private String upEmpno;
    private String buyGubn;
    private String newCsType;
    private String newCsSubType;
    private String buyType;

    private String inTime;

    private String custTell;

    private String countRe;
    private String projectName;

    private String completionCode;

    public String getCompletionCode() {
        return completionCode;
    }

    public void setCompletionCode(String completionCode) {
        this.completionCode = completionCode;
    }

    private Consultation consultation;
    private List<Comment> comments;

    public Consultation getConsultation() {
        return consultation;
    }

    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCountRe() {
        return countRe;
    }

    public void setCountRe(String countRe) {
        this.countRe = countRe;
    }

    public String getCustTell() {
        return custTell;
    }

    public void setCustTell(String custTell) {
        this.custTell = custTell;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    // Getters and Setters
    // 필요에 따라 toString, equals, hashCode 등의 메소드를 오버라이드 할 수 있습니다.

    public String getRingTime() {
        return ringTime;
    }

    public void setRingTime(String ringTime) {
        this.ringTime = ringTime;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    public String getCsDate() {
        return csDate;
    }

    public void setCsDate(String csDate) {
        this.csDate = csDate;
    }

    public String getCsCash() {
        return csCash;
    }

    public void setCsCash(String csCash) {
        this.csCash = csCash;
    }

    public String getInDate() {
        return inDate;
    }

    public void setInDate(String inDate) {
        this.inDate = inDate;
    }

    public String getUp_Date() {
        return up_Date;
    }

    public void setUp_Date(String up_Date) {
        this.up_Date = up_Date;
    }

    // 예: Getters
    public String getCustCode() {
        return custCode;
    }

    // 예: Setters
    public void setCustCode(String custCode) {
        this.custCode = custCode;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getPersonCode() {
        return personCode;
    }

    public void setPersonCode(String personCode) {
        this.personCode = personCode;
    }

    public String getCallCode() {
        return callCode;
    }

    public void setCallCode(String callCode) {
        this.callCode = callCode;
    }

    public String getEmpNo() {
        return empNo;
    }

    public void setEmpNo(String empNo) {
        this.empNo = empNo;
    }

    public String getIncallNo() {
        return incallNo;
    }

    public void setIncallNo(String incallNo) {
        this.incallNo = incallNo;
    }

    public String getOutcallNo() {
        return outcallNo;
    }

    public void setOutcallNo(String outcallNo) {
        this.outcallNo = outcallNo;
    }



    public String getRingId() {
        return ringId;
    }

    public void setRingId(String ringId) {
        this.ringId = ringId;
    }

    public Date getChannelTime() {
        return channelTime;
    }

    public void setChannelTime(Date channelTime) {
        this.channelTime = channelTime;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }


    public String getOutId() {
        return outId;
    }

    public void setOutId(String outId) {
        this.outId = outId;
    }

    public String getCsType() {
        return csType;
    }

    public void setCsType(String csType) {
        this.csType = csType;
    }

    public String getCsNote() {
        return csNote;
    }

    public void setCsNote(String csNote) {
        this.csNote = csNote;
    }



    public String getCsBank() {
        return csBank;
    }

    public void setCsBank(String csBank) {
        this.csBank = csBank;
    }

    public String getCsName() {
        return csName;
    }

    public void setCsName(String csName) {
        this.csName = csName;
    }


    public String getPrcGubn() {
        return prcGubn;
    }

    public void setPrcGubn(String prcGubn) {
        this.prcGubn = prcGubn;
    }

    public String getPrcNote() {
        return prcNote;
    }

    public void setPrcNote(String prcNote) {
        this.prcNote = prcNote;
    }

    public String getEmgGubn() {
        return emgGubn;
    }

    public void setEmgGubn(String emgGubn) {
        this.emgGubn = emgGubn;
    }

    public String getRecallEmp() {
        return recallEmp;
    }

    public void setRecallEmp(String recallEmp) {
        this.recallEmp = recallEmp;
    }

    public String getRecallCust() {
        return recallCust;
    }

    public void setRecallCust(String recallCust) {
        this.recallCust = recallCust;
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


    public String getInEmpno() {
        return inEmpno;
    }

    public void setInEmpno(String inEmpno) {
        this.inEmpno = inEmpno;
    }


    public String getUpEmpno() {
        return upEmpno;
    }

    public void setUpEmpno(String upEmpno) {
        this.upEmpno = upEmpno;
    }

    public String getBuyGubn() {
        return buyGubn;
    }

    public void setBuyGubn(String buyGubn) {
        this.buyGubn = buyGubn;
    }

    public String getNewCsType() {
        return newCsType;
    }

    public void setNewCsType(String newCsType) {
        this.newCsType = newCsType;
    }

    public String getNewCsSubType() {
        return newCsSubType;
    }

    public void setNewCsSubType(String newCsSubType) {
        this.newCsSubType = newCsSubType;
    }

    public String getBuyType() {
        return buyType;
    }

    public void setBuyType(String buyType) {
        this.buyType = buyType;
    }
// 기타 필드에 대한 Getters 및 Setters
}

