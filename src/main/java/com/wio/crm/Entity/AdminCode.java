package com.wio.crm.Entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsys01")
public class AdminCode {
    @Id
    private String admCode;
    private String admGubn;
    private String admSname;

    // Getters and Setters
    public String getAdmCode() {
        return admCode;
    }

    public void setAdmCode(String admCode) {
        this.admCode = admCode;
    }

    public String getAdmGubn() {
        return admGubn;
    }

    public void setAdmGubn(String admGubn) {
        this.admGubn = admGubn;
    }

    public String getAdmSname() {
        return admSname;
    }

    public void setAdmSname(String admSname) {
        this.admSname = admSname;
    }

    @Override
    public String toString() {
        return "AdminCode{" +
                "admCode='" + admCode + '\'' +
                ", admGubn='" + admGubn + '\'' +
                ", admSname='" + admSname + '\'' +
                '}';
    }
}
