package com.wio.crm.model;

public class Tcnt01Emp {

    public Tcnt01Emp(String custCode, String empno, String use_yn, String emp_name, String id , String cust_gubn) {
        this.custCode = custCode;
        this.empno = empno;
        this.use_yn = use_yn;
        this.emp_name = emp_name;
        this.id = id;
        this.cust_gubn=cust_gubn;
    }
    private String custCode;
    private String empno;
    // ... (Other fields)
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

    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
                // Include other fields here
                '}';
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



    // Getters and Setters
    // CustCode
    public String getCustCode() { return custCode; }
    public void setCustCode(String custCode) { this.custCode = custCode; }

    // EmpNo
    public String getEmpno() { return empno; }
    public void setEmpno(String empno) { this.empno = empno; }

    // ... (Other getters and setters)
}
