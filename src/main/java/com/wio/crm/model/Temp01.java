package com.wio.crm.model;

public class Temp01 {

    public Temp01(String empno, String userId, String call_yn, String work_gubn, String position, String emp_Name) {
        this.empno = empno;
        this.userId = userId;
        this.call_yn = call_yn;
        this.work_gubn = work_gubn;
        this.position = position;
        this.emp_Name = emp_Name;


    }
    private String empno;
    private String emp_Name;
    // ... (Other fields)
    private String saup_gubn;
    private String id;
    private String pw;
    private String call_no;
    private String depart;
    private String position;
    private String zip_no;
    private String addr;
    private String tel_no;
    private String mobile;
    private String birth_day;
    private String email;
    private String merry_gubn;
    private String call_yn;
    private String work_gubn;
    private String in_date;
    private String in_empNo;
    private String up_date;
    private String up_empNo;
    private String addr2;
    private String subId;
    private String con_time;
    private String userId;

    public String getEmp_Name() {
        return emp_Name;
    }

    public void setEmp_Name(String emp_Name) {
        this.emp_Name = emp_Name;
    }

    private boolean isAdmin; // 관리자 여부를 나타내는 필드

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    // Getters and Setters
    // EmpNo
    public String getEmpno() { return empno; }
    public void setEmpno(String empno) { this.empno = empno; }

    // EmpName

    // ... (Other getters and setters)

    public String getSaup_gubn() {
        return saup_gubn;
    }

    public void setSaup_gubn(String saup_gubn) {
        this.saup_gubn = saup_gubn;
    }

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

    public String getCall_no() {
        return call_no;
    }

    public void setCall_no(String call_no) {
        this.call_no = call_no;
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

    public String getZip_no() {
        return zip_no;
    }

    public void setZip_no(String zip_no) {
        this.zip_no = zip_no;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getTel_no() {
        return tel_no;
    }

    public void setTel_no(String tel_no) {
        this.tel_no = tel_no;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getBirth_day() {
        return birth_day;
    }

    public void setBirth_day(String birth_day) {
        this.birth_day = birth_day;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMerry_gubn() {
        return merry_gubn;
    }

    public void setMerry_gubn(String merry_gubn) {
        this.merry_gubn = merry_gubn;
    }

    public String getCall_yn() {
        return call_yn;
    }

    public void setCall_yn(String call_yn) {
        this.call_yn = call_yn;
    }

    public String getWork_gubn() {
        return work_gubn;
    }

    public void setWork_gubn(String work_gubn) {
        this.work_gubn = work_gubn;
    }

    public String getIn_date() {
        return in_date;
    }

    public void setIn_date(String in_date) {
        this.in_date = in_date;
    }

    public String getIn_empNo() {
        return in_empNo;
    }

    public void setIn_empNo(String in_empNo) {
        this.in_empNo = in_empNo;
    }

    public String getUp_date() {
        return up_date;
    }

    public void setUp_date(String up_date) {
        this.up_date = up_date;
    }

    public String getUp_empNo() {
        return up_empNo;
    }

    public void setUp_empNo(String up_empNo) {
        this.up_empNo = up_empNo;
    }

    public String getAddr2() {
        return addr2;
    }

    public void setAddr2(String addr2) {
        this.addr2 = addr2;
    }

    public String getSubId() {
        return subId;
    }

    public void setSubId(String subId) {
        this.subId = subId;
    }

    public String getCon_time() {
        return con_time;
    }

    public void setCon_time(String con_time) {
        this.con_time = con_time;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
