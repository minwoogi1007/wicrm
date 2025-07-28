package com.wio.crm.service;

import com.wio.crm.config.CustomUserDetails;
import com.wio.crm.mapper.AccountMapper;
import com.wio.crm.model.Account;
import com.wio.crm.model.Tcnt01Emp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("🔍 [AccountService] Authentication 객체: {}", authentication != null ? "존재" : "null");
        
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            logger.warn("❌ [AccountService] Authentication이 null이거나 CustomUserDetails가 아님");
            return ""; // Early return for null or incorrect type
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        logger.info("🔍 [AccountService] CustomUserDetails: {}", userDetails);
        
        // tcntUserInfo와 tempUserInfo 상태 확인
        if (userDetails.getTcntUserInfo() != null) {
            String userId = userDetails.getTcntUserInfo().getUserId();
            logger.info("✅ [AccountService] TCNT 사용자 ID: {}", userId);
            return userId;
        } else if (userDetails.getTempUserInfo() != null) {
            String userId = userDetails.getTempUserInfo().getUserId();
            logger.info("✅ [AccountService] TEMP 사용자 ID: {}", userId);
            return userId;
        } else {
            logger.warn("❌ [AccountService] TcntUserInfo와 TempUserInfo 모두 null");
            return "";
        }
    }

    public Map<String, Object> getAccount() {
        Map<String, Object> data = new HashMap<>();
        String userId = getCurrentUserId();
        logger.info("🔍 [AccountService] 조회할 사용자 ID: '{}'", userId);

        if (userId == null || userId.trim().isEmpty()) {
            logger.error("❌ [AccountService] 사용자 ID가 null이거나 빈 값");
            data.put("accountInfo", null);
            return data;
        }

        try {
            logger.info("🔍 [AccountService] AccountMapper.getAccount 호출 시작 - userId: '{}'", userId);
            Tcnt01Emp accountInfo = accountMapper.getAccount(userId);
            logger.info("🔍 [AccountService] AccountMapper.getAccount 결과: {}", accountInfo);

            if (accountInfo != null) {
                logger.info("✅ [AccountService] 계정 정보 조회 성공:");
                logger.info("   - 사용자 ID: {}", accountInfo.getUserId());
                logger.info("   - 직원명: {}", accountInfo.getEmp_name());
                logger.info("   - 고객 코드: {}", accountInfo.getCustCode());
                logger.info("   - 🏢 회사명: '{}'", accountInfo.getCust_name());
                logger.info("   - 홈페이지: {}", accountInfo.getHomePage());
                logger.info("   - 전화번호: {}", accountInfo.getTel_no());
                logger.info("   - 주소: {}", accountInfo.getAddr());
            } else {
                logger.error("❌ [AccountService] 계정 정보 조회 결과가 null - userId: '{}'", userId);
            }

            data.put("accountInfo", accountInfo);
        } catch (Exception e) {
            logger.error("💥 [AccountService] 계정 정보 조회 중 오류 발생 - userId: '{}', 오류: {}", userId, e.getMessage(), e);
            data.put("accountInfo", null);
        }

        return data;
    }

    @Transactional
    public boolean updateAccount(Account account) {
        String userId = getCurrentUserId();
        account.setUserId(userId);
        logger.info("Updating account: {}", account);
        accountMapper.updateAccount(account);
        return accountMapper.updateAccount(account) > 0;
    }

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public boolean checkCurrentPassword(String currentPassword) {
        String userId = getCurrentUserId();
        Account account = accountMapper.findUserByUsername(userId);
        return passwordEncoder.matches(currentPassword, account.getPassword());
    }
    
    @Transactional
    public void changeUserPassword(String newPassword) {
        String userId = getCurrentUserId();
        String encodedPassword = passwordEncoder.encode(newPassword);
        accountMapper.updateUserPassword(userId, encodedPassword);
    }
}
