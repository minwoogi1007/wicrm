package com.wio.crm.model;

import java.time.LocalDateTime;

public class UserApproval {
    private String id;
    private String pw;
    private String gubn;
    private LocalDateTime insertTime;
    private String confirmYn;
    private LocalDateTime confirmTime;
    private String confirmEmp;
    private String userid;
    private String username;
    private String repass;

    // Additional fields for N_TCNT01_EMP_TEMP
    private String empNo;
    private String empName;
    private String depart;
    private String position;
    private String zipNo;
    private String addr;
    private String telNo;
    private String fexNo;
    private String handPhone;
    private String email;
    private String rmk;
    private String useYn;
    private LocalDateTime inDate;
    private String inEmpno;
    private LocalDateTime upDate;
    private String upEmpno;
    private String custName;
    private String addr2;

    private String custCode;

    public String getCustCode() {
        return custCode;
    }

    public void setCustCode(String custCode) {
        this.custCode = custCode;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getGubn() {
        return gubn;
    }

    public void setGubn(String gubn) {
        this.gubn = gubn;
    }

    public LocalDateTime getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(LocalDateTime insertTime) {
        this.insertTime = insertTime;
    }

    public String getConfirmYn() {
        return confirmYn;
    }

    public void setConfirmYn(String confirmYn) {
        this.confirmYn = confirmYn;
    }

    public LocalDateTime getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(LocalDateTime confirmTime) {
        this.confirmTime = confirmTime;
    }

    public String getConfirmEmp() {
        return confirmEmp;
    }

    public void setConfirmEmp(String confirmEmp) {
        this.confirmEmp = confirmEmp;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRepass() {
        return repass;
    }

    public void setRepass(String repass) {
        this.repass = repass;
    }

    public String getEmpNo() {
        return empNo;
    }

    public void setEmpNo(String empNo) {
        this.empNo = empNo;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getZipNo() {
        return zipNo;
    }

    public void setZipNo(String zipNo) {
        this.zipNo = zipNo;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getTelNo() {
        return telNo;
    }

    public void setTelNo(String telNo) {
        this.telNo = telNo;
    }

    public String getFexNo() {
        return fexNo;
    }

    public void setFexNo(String fexNo) {
        this.fexNo = fexNo;
    }

    public String getHandPhone() {
        return handPhone;
    }

    public void setHandPhone(String handPhone) {
        this.handPhone = handPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRmk() {
        return rmk;
    }

    public void setRmk(String rmk) {
        this.rmk = rmk;
    }

    public String getUseYn() {
        return useYn;
    }

    public void setUseYn(String useYn) {
        this.useYn = useYn;
    }

    public LocalDateTime getInDate() {
        return inDate;
    }

    public void setInDate(LocalDateTime inDate) {
        this.inDate = inDate;
    }

    public String getInEmpno() {
        return inEmpno;
    }

    public void setInEmpno(String inEmpno) {
        this.inEmpno = inEmpno;
    }

    public LocalDateTime getUpDate() {
        return upDate;
    }

    public void setUpDate(LocalDateTime upDate) {
        this.upDate = upDate;
    }

    public String getUpEmpno() {
        return upEmpno;
    }

    public void setUpEmpno(String upEmpno) {
        this.upEmpno = upEmpno;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getAddr2() {
        return addr2;
    }

    public void setAddr2(String addr2) {
        this.addr2 = addr2;
    }

    @Override
    public String toString() {
        return "UserApproval{" +
                "id='" + id + '\'' +
                ", userid='" + userid + '\'' +
                ", username='" + username + '\'' +
                ", confirmYn='" + confirmYn + '\'' +
                ", insertTime=" + insertTime +
                ", custName='" + custName + '\'' +
                '}';
    }
}