package com.wio.crm.model;

import org.apache.ibatis.annotations.Mapper;


public class Transaction {
    private String date;
    private String type;
    private double amount;
    private String description;

    private String PRTN_CD;
    private String CUST_NAME;
    private String CHARGE_DATE;
    private String CHARGE_SERL_NO;
    private String CHARGE_POINT;
    private String CHARGE_GUBN;
    private String CHARGE_SP;
    private String  CHARGE_PSTOR_CD;
    private String CHARGE_EMPNO;
    private String CHARGE_PRO_CD;
    private String CHARGE_SITE_CD;
    private String CHARGE_CALL_CD;
    private String CHARGE_INS_DATE;
    private String CHARGE_INS_EMPNO;
    private String POINT_CHARGE;

    // 기본 생성자 추가
    public Transaction() {
        // 기본 생성자
    }

    // Constructors, getters, and setters
    public Transaction(String date, String type, double amount, String description) {
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.description = description;
    }

    public String getPRTN_CD() {
        return PRTN_CD;
    }

    public void setPRTN_CD(String PRTN_CD) {
        this.PRTN_CD = PRTN_CD;
    }

    public String getCUST_NAME() {
        return CUST_NAME;
    }

    public void setCUST_NAME(String CUST_NAME) {
        this.CUST_NAME = CUST_NAME;
    }

    public String getCHARGE_DATE() {
        return CHARGE_DATE;
    }

    public void setCHARGE_DATE(String CHARGE_DATE) {
        this.CHARGE_DATE = CHARGE_DATE;
    }

    public String getCHARGE_SERL_NO() {
        return CHARGE_SERL_NO;
    }

    public void setCHARGE_SERL_NO(String CHARGE_SERL_NO) {
        this.CHARGE_SERL_NO = CHARGE_SERL_NO;
    }

    public String getCHARGE_POINT() {
        return CHARGE_POINT;
    }

    public void setCHARGE_POINT(String CHARGE_POINT) {
        this.CHARGE_POINT = CHARGE_POINT;
    }

    public String getCHARGE_GUBN() {
        return CHARGE_GUBN;
    }

    public void setCHARGE_GUBN(String CHARGE_GUBN) {
        this.CHARGE_GUBN = CHARGE_GUBN;
    }

    public String getCHARGE_SP() {
        return CHARGE_SP;
    }

    public void setCHARGE_SP(String CHARGE_SP) {
        this.CHARGE_SP = CHARGE_SP;
    }

    public String getCHARGE_PSTOR_CD() {
        return CHARGE_PSTOR_CD;
    }

    public void setCHARGE_PSTOR_CD(String CHARGE_PSTOR_CD) {
        this.CHARGE_PSTOR_CD = CHARGE_PSTOR_CD;
    }

    public String getCHARGE_EMPNO() {
        return CHARGE_EMPNO;
    }

    public void setCHARGE_EMPNO(String CHARGE_EMPNO) {
        this.CHARGE_EMPNO = CHARGE_EMPNO;
    }

    public String getCHARGE_PRO_CD() {
        return CHARGE_PRO_CD;
    }

    public void setCHARGE_PRO_CD(String CHARGE_PRO_CD) {
        this.CHARGE_PRO_CD = CHARGE_PRO_CD;
    }

    public String getCHARGE_SITE_CD() {
        return CHARGE_SITE_CD;
    }

    public void setCHARGE_SITE_CD(String CHARGE_SITE_CD) {
        this.CHARGE_SITE_CD = CHARGE_SITE_CD;
    }

    public String getCHARGE_CALL_CD() {
        return CHARGE_CALL_CD;
    }

    public void setCHARGE_CALL_CD(String CHARGE_CALL_CD) {
        this.CHARGE_CALL_CD = CHARGE_CALL_CD;
    }

    public String getCHARGE_INS_DATE() {
        return CHARGE_INS_DATE;
    }

    public void setCHARGE_INS_DATE(String CHARGE_INS_DATE) {
        this.CHARGE_INS_DATE = CHARGE_INS_DATE;
    }

    public String getCHARGE_INS_EMPNO() {
        return CHARGE_INS_EMPNO;
    }

    public void setCHARGE_INS_EMPNO(String CHARGE_INS_EMPNO) {
        this.CHARGE_INS_EMPNO = CHARGE_INS_EMPNO;
    }

    public String getPOINT_CHARGE() {
        return POINT_CHARGE;
    }

    public void setPOINT_CHARGE(String POINT_CHARGE) {
        this.POINT_CHARGE = POINT_CHARGE;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    // 객체 디버깅을 위한 toString 메서드 추가
    @Override
    public String toString() {
        return "Transaction{" +
                "PRTN_CD='" + PRTN_CD + '\'' +
                ", CUST_NAME='" + CUST_NAME + '\'' +
                ", CHARGE_DATE='" + CHARGE_DATE + '\'' +
                ", CHARGE_SERL_NO='" + CHARGE_SERL_NO + '\'' +
                ", CHARGE_POINT='" + CHARGE_POINT + '\'' +
                ", CHARGE_GUBN='" + CHARGE_GUBN + '\'' +
                ", POINT_CHARGE='" + POINT_CHARGE + '\'' +
                '}';
    }
}
