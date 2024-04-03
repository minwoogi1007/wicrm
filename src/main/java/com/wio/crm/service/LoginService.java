package com.wio.crm.service;

import com.wio.crm.mapper.UserMapper;
import com.wio.crm.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    public void applyUserId(UserInfo userInfo) {
        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(userInfo.getPassword());
        // 암호화된 비밀번호로 userInfo 객체 업데이트
        userInfo.setPassword(encryptedPassword);
        // 데이터베이스에 저장
        System.out.println(userInfo);
        userMapper.insertUser(userInfo);
    }
}
