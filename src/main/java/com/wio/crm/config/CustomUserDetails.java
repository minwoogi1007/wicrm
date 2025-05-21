package com.wio.crm.config;

import com.wio.crm.model.Tcnt01Emp;
import com.wio.crm.model.Temp01;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;

public class CustomUserDetails extends User {
    private String custCode;
    private Temp01 tempUserInfo; // 내부직원 정보
    private Tcnt01Emp tcntUserInfo; // 거래처 직원 정보

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, String custCode, Temp01 tempUserInfo, Tcnt01Emp tcntUserInfo) {
        super(username, password, authorities);
        this.custCode = custCode;
        this.tempUserInfo = tempUserInfo;
        this.tcntUserInfo = tcntUserInfo;
    }

    public String getCustCode() {
        return custCode;
    }

    public Temp01 getTempUserInfo() {
        return tempUserInfo;
    }

    public void setTempUserInfo(Temp01 tempUserInfo) {
        this.tempUserInfo = tempUserInfo;
    }

    public Tcnt01Emp getTcntUserInfo() {
        return tcntUserInfo;
    }

    public void setTcntUserInfo(Tcnt01Emp tcntUserInfo) {
        this.tcntUserInfo = tcntUserInfo;
    }
    
    @Override
    public String toString() {
        return "CustomUserDetails{" +
                "username=" + getUsername() +
                ", custCode='" + custCode + '\'' +
                ", tempUserInfo=" + (tempUserInfo != null ? tempUserInfo.toString() : "null") +
                ", tcntUserInfo=" + (tcntUserInfo != null ? tcntUserInfo.toString() : "null") +
                '}';
    }
}