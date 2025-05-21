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
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ""; // Early return for null or incorrect type
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Using a ternary operator to simplify code
        return userDetails.getTcntUserInfo() != null ? userDetails.getTcntUserInfo().getUserId() : "";
    }

    public Map<String, Object> getAccount() {
        Map<String, Object> data = new HashMap<>();
        String userId = getCurrentUserId();
        logger.debug("Fetching account for custCode: {}", userId);

        try {
            Tcnt01Emp accountInfo = accountMapper.getAccount(userId);
            logger.debug("Account info retrieved: {}", accountInfo);

            if (accountInfo != null) {
                logger.debug("Detailed Account Info: {}", accountInfo);
            } else {
                logger.debug("No account found for userId: {}", userId);
            }

            data.put("accountInfo", accountInfo);
        } catch (Exception e) {
            logger.error("Error fetching account info for userId: {}", userId, e);
        }

        return data;
    }
    @Transactional
    public boolean updateAccount(Account account) {

        String userId = getCurrentUserId();
        account.setUserId(userId);
        // Assuming AccountRepository extends JpaRepository
        logger.info("Updating account: {}", account);
        accountMapper.updateAccount(account);
        return accountMapper.updateAccount(account) > 0;

    }

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public boolean checkCurrentPassword(String currentPassword) {
        String userId = getCurrentUserId();

        Account account = accountMapper.findUserByUsername(userId);
        //System.out.println("user==========="+account.getPassword());

        return passwordEncoder.matches(currentPassword, account.getPassword());
    }
    @Transactional
    public void changeUserPassword(String newPassword) {
        String userId = getCurrentUserId();
        String encodedPassword = passwordEncoder.encode(newPassword);

        accountMapper.updateUserPassword(userId, encodedPassword);
    }
}
