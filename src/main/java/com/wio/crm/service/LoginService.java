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
        // gubn에 따른 처리 로직
        if (userInfo.getGubn().equals("0")) {
            // 업체 직원인 경우

            // n_tcnt01_emp_temp 임시 테이블에 등록
            userMapper.insertTcnt01Emp(userInfo);
        } else if (userInfo.getGubn().equals("1")) {
            // 직원인 경우

            // n_temp01 테이블에 추가 등록
            userMapper.insertTemp01(userInfo);
        } else {
            // 예외 처리 또는 기타 처리
            throw new IllegalArgumentException("잘못된 gubn 값입니다.");
        }
//

    }
}
