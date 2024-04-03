package com.wio.crm.model;

public class UserInfo {
    private String userId;
    private String password;
    private String userName;
    private String  gubn;
    // 기본 생성자
    public UserInfo() {
    }

    // 모든 필드를 포함하는 생성자
    public UserInfo(String userId, String password,String userName , String gubn) {
        this.userId = userId;
        this.password = password;
        this.userName = userName;
        this.gubn = gubn;
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
