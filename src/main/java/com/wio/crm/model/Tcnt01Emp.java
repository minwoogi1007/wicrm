package com.wio.crm.model;

public class Tcnt01Emp {

    private String custCode;
    private String empno;
    private String use_yn;
    private String emp_name;
    private String userId;
    private String id;
    private String cust_gubn;
    private String cust_grade;
    private String tel_no;
    private String hand_phone;
    private String email;
    private String homePage;
    private String cust_name;
    private int hasEmail;
    private int hasPhone;
    private String admin;
    private String authority;

    // 📝 AccountMapper.xml에서 SELECT하는 누락된 필드들 추가
    private String depart;      // A.DEPART
    private String position;    // A.POSITION
    private String zip_no;      // A.ZIP_NO
    private String addr;        // A.ADDR
    private String fex_no;      // A.FEX_NO
    private String rmk;         // A.RMK

    // ===== 기존 getter/setter 메서드들 =====

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public int getHasEmail() {
        return hasEmail;
    }

    public void setHasEmail(int hasEmail) {
        this.hasEmail = hasEmail;
    }

    public int getHasPhone() {
        return hasPhone;
    }

    public void setHasPhone(int hasPhone) {
        this.hasPhone = hasPhone;
    }

    public String getTel_no() {
        return tel_no;
    }

    public void setTel_no(String tel_no) {
        this.tel_no = tel_no;
    }

    public String getHand_phone() {
        return hand_phone;
    }

    public void setHand_phone(String hand_phone) {
        this.hand_phone = hand_phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHomePage() {
        return homePage;
    }

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }

    public String getCust_name() {
        return cust_name;
    }

    public void setCust_name(String cust_name) {
        this.cust_name = cust_name;
    }

    public String getCust_grade() {
        return cust_grade;
    }

    public void setCust_grade(String cust_grade) {
        this.cust_grade = cust_grade;
    }

    public String getCust_gubn() {
        return cust_gubn;
    }

    public void setCust_gubn(String cust_gubn) {
        this.cust_gubn = cust_gubn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUse_yn() {
        return use_yn;
    }

    public void setUse_yn(String use_yn) {
        this.use_yn = use_yn;
    }

    public String getEmp_name() {
        return emp_name;
    }

    public void setEmp_name(String emp_name) {
        this.emp_name = emp_name;
    }

    public String getCustCode() { 
        return custCode; 
    }
    
    public void setCustCode(String custCode) { 
        this.custCode = custCode; 
    }

    public String getEmpno() { 
        return empno; 
    }
    
    public void setEmpno(String empno) { 
        this.empno = empno; 
    }

    // ===== 🆕 새로 추가된 필드들의 getter/setter =====

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

    public String getFex_no() {
        return fex_no;
    }

    public void setFex_no(String fex_no) {
        this.fex_no = fex_no;
    }

    public String getRmk() {
        return rmk;
    }

    public void setRmk(String rmk) {
        this.rmk = rmk;
    }

    @Override
    public String toString() {
        return "Tcnt01Emp{" +
                "userId='" + userId + '\'' +
                ", emp_name='" + emp_name + '\'' +
                ", email='" + email + '\'' +
                ", handPhone='" + hand_phone + '\'' +
                ", homePage='" + homePage + '\'' +
                ", tel_no='" + tel_no + '\'' +
                ", cust_name='" + cust_name + '\'' +
                ", custCode='" + custCode + '\'' +
                '}';
    }
}
