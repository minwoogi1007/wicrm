package com.wio.crm.model;

public class Tipdw {
    private String id;
    private String pw;
    private String gubn;
    private String insertTime;
    private String confirmYn;
    private String confirmTime;
    private String confirmEmp;
    private String userid;
    private String username;

    // 기본 생성자


    // 모든 필드를 포함하는 생성자
    public Tipdw(String userid, String pw, String username, String gubn, String confirmYn) {
        this.userid = userid;
        this.pw = pw;
        this.username = username;
        this.gubn = gubn;
        this.confirmYn = confirmYn;
    }

    // Getters and Setters
    // ID
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // Password
    public String getPw() { return pw; }
    public void setPw(String pw) { this.pw = pw; }

    // ... (Other getters and setters)

    public String getGubn() {
        return gubn;
    }

    public void setGubn(String gubn) {
        this.gubn = gubn;
    }

    public String getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(String insertTime) {
        this.insertTime = insertTime;
    }

    public String getConfirmYn() {
        return confirmYn;
    }

    public void setConfirmYn(String confirmYn) {
        this.confirmYn = confirmYn;
    }

    public String getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(String confirmTime) {
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
    // toString() 메서드는 로깅이나 디버깅 목적으로 유용할 수 있습니다.
    @Override
    public String toString() {
        return "Tipdw{" +
                "userid='" + userid + '\'' +
                ", pw='" + pw + '\'' +
                ", username='" + username + '\'' +
                ", gubn='" + gubn + '\'' +
                ", confirmYn='" + confirmYn + '\'' +
                '}';
    }
}
