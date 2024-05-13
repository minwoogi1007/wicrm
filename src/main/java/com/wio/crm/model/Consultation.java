package com.wio.crm.model;

import java.math.BigDecimal;
import java.util.Date;

public class Consultation {
    private String custCode;
    private String projectCode;
    private String personCode;
    private String callCode;
    private String empNo;
    private String incallNo;
    private String outcallNo;
    private Date ringTime;
    private String ringId;
    private Date channelTime;
    private String channelId;
    private Date outTime;
    private String outId;
    private String csType;
    private String csNote;
    private Date csDate;
    private String csBank;
    private String csName;
    private BigDecimal csCash;
    private String prcGubn;
    private String prcNote;
    private String emgGubn;
    private String recallEmp;
    private String recallCust;
    private String saupGubn;
    private String delGubn;
    private Date inDate;
    private String inEmpno;
    private Date upDate;
    private String upEmpno;
    private String buyGubn;
    private String newCsType;
    private String newCsSubType;
    private String buyType;

    // Getters and Setters
    // 필요에 따라 toString, equals, hashCode 등의 메소드를 오버라이드 할 수 있습니다.

    // 예: Getters
    public String getCustCode() {
        return custCode;
    }

    // 예: Setters
    public void setCustCode(String custCode) {
        this.custCode = custCode;
    }

    // 기타 필드에 대한 Getters 및 Setters
}

