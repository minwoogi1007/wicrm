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
