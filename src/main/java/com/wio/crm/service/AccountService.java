package com.wio.crm.service;

import com.wio.crm.config.CustomUserDetails;
import com.wio.crm.mapper.AccountMapper;
import com.wio.crm.model.Account;
import com.wio.crm.model.Mileage;
import com.wio.crm.model.Tcnt01Emp;
import com.wio.crm.model.Transaction;
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
        System.out.println("custCode-===" + userId);

        Tcnt01Emp accountInfo = accountMapper.getAccount(userId);

        data.put("accountInfo", accountInfo);

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

        Tcnt01Emp user = accountMapper.findUserByUsername(userId);
        System.out.println("user==========="+user.getPassword());

        return passwordEncoder.matches(currentPassword, user.getPassword());
    }
    @Transactional
    public void changeUserPassword(String newPassword) {
        String userId = getCurrentUserId();
        String encodedPassword = passwordEncoder.encode(newPassword);

        accountMapper.updateUserPassword(userId, encodedPassword);
    }
}
