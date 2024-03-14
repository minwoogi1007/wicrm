package com.wio.crm.service;

import com.wio.crm.mapper.LoginHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginHistoryService {

    private final LoginHistoryMapper loginHistoryMapper;

    @Autowired
    public LoginHistoryService(LoginHistoryMapper loginHistoryMapper) {
        this.loginHistoryMapper = loginHistoryMapper;
    }

    public void recordLoginHistory(String userId) {
        // MyBatis Mapper를 사용하여 로그인 이력을 데이터베이스에 기록
        loginHistoryMapper.insertLoginHistory(userId);
    }
}
