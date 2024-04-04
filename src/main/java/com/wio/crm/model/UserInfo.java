package com.wio.crm.model;

public class UserInfo {
    private String userId;
    private String password;
    private String userName;
    private String  gubn;

    private String companyName;
    // 기본 생성자
    public UserInfo() {
    }

    // 모든 필드를 포함하는 생성자
    public UserInfo(String userId, String password,String userName , String gubn,String companyName) {
        this.userId = userId;
        this.password = password;
        this.userName = userName;
        this.gubn = gubn;
        this.companyName = companyName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getGubn() {
        return gubn;
    }

    public void setGubn(String gubn) {
        this.gubn = gubn;
    }

    // Getter와 Setter
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    // toString() 메소드 (선택적)
    @Override
    public String toString() {
        return "UserInfo{" +
                "userId='" + userId + '\'' +
                ", password='" + password + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
