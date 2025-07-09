package com.wio.crm.service;

import com.wio.crm.Entity.UserActionLog;
import com.wio.crm.dto.UserActionLogDTO;
import com.wio.crm.repository.UserActionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserActionLogService {
    
    private final UserActionLogRepository logRepository;
    
    @Autowired
    public UserActionLogService(UserActionLogRepository logRepository) {
        this.logRepository = logRepository;
    }
    
    @Transactional
    public void saveUserActionLog(UserActionLogDTO logDTO) {
        UserActionLog log = new UserActionLog();
        log.setUserId(logDTO.getUserId());
        log.setActionType(logDTO.getActionType());
        log.setActionTarget(logDTO.getActionTarget());
        log.setActionUrl(logDTO.getActionUrl());
        log.setActionData(logDTO.getActionData());
        log.setIpAddress(logDTO.getIpAddress());
        log.setUserAgent(logDTO.getUserAgent());
        log.setSessionId(logDTO.getSessionId());
        
        if (logDTO.getActionTimestamp() != null) {
            log.setActionTimestamp(logDTO.getActionTimestamp());
        } else {
            log.setActionTimestamp(LocalDateTime.now());
        }
        
        logRepository.save(log);
    }
    
    @Transactional(readOnly = true)
    public List<UserActionLog> getUserActionLogs(String userId) {
        return logRepository.findByUserIdOrderByActionTimestampDesc(userId);
    }
    
    @Transactional(readOnly = true)
    public List<UserActionLog> getUserActionLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return logRepository.findByActionTimestampBetween(start, end);
    }
    
    @Transactional(readOnly = true)
    public List<UserActionLog> getUserActionLogsByType(String actionType, String userId) {
        return logRepository.findByActionTypeAndUserIdOrderByActionTimestampDesc(actionType, userId);
    }
} 