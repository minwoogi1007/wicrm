package com.wio.crm.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;

public class CustomUserDetails extends User {
    private String custCode;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, String custCode) {
        super(username, password, authorities);
        this.custCode = custCode;
    }

    public String getCustCode() {
        return custCode;
    }

    // 필요에 따라 추가 메서드 구현
}