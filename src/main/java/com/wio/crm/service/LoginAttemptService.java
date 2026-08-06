package com.wio.crm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    private final ConcurrentHashMap<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();

    public void loginFailed(String key) {
        AttemptInfo info = attemptsCache.getOrDefault(key, new AttemptInfo());

        if (info.lockExpiry != null && LocalDateTime.now().isAfter(info.lockExpiry)) {
            info = new AttemptInfo();
        }

        info.attempts++;
        if (info.attempts >= MAX_ATTEMPTS) {
            info.lockExpiry = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
            logger.warn("계정/IP 잠금 처리 - key: {}, {}분간 차단", key, LOCK_MINUTES);
        }
        attemptsCache.put(key, info);
    }

    public void loginSucceeded(String key) {
        attemptsCache.remove(key);
    }

    public boolean isBlocked(String key) {
        AttemptInfo info = attemptsCache.get(key);
        if (info == null) return false;

        if (info.lockExpiry != null) {
            if (LocalDateTime.now().isAfter(info.lockExpiry)) {
                attemptsCache.remove(key);
                return false;
            }
            return true;
        }
        return false;
    }

    public int getRemainingAttempts(String key) {
        AttemptInfo info = attemptsCache.get(key);
        if (info == null) return MAX_ATTEMPTS;
        return Math.max(0, MAX_ATTEMPTS - info.attempts);
    }

    private static class AttemptInfo {
        int attempts = 0;
        LocalDateTime lockExpiry = null;
    }
}
