package com.wio.crm.model;

public class Account {
    private Long id;
    private String emp_name;
    private String email;
    private String hand_phone;
    private String homePage;
    private String userId;

    private String cust_name;

    private String password;

    private int hasEmail;
    private int hasPhone;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCust_name() {
        return cust_name;
    }

    public void setCust_name(String cust_name) {
        this.cust_name = cust_name;
    }

    // Getters and Setters

    public String getEmp_name() {
        return emp_name;
    }

    public void setEmp_name(String emp_name) {
        this.emp_name = emp_name;
    }

    public String getHand_phone() {
        return hand_phone;
    }

    public void setHand_phone(String hand_phone) {
        this.hand_phone = hand_phone;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", emp_name='" + emp_name + '\'' +
                ", email='" + email + '\'' +
                ", hand_phone='" + hand_phone + '\'' +
                ", homePage='" + homePage + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
