package com.wio.crm.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER_ACTION_LOG")
public class UserActionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOG_SEQ")
    @SequenceGenerator(name = "LOG_SEQ", sequenceName = "LOG_ID_SEQ", allocationSize = 1)
    @Column(name = "LOG_ID")
    private Long id;
    
    @Column(name = "USER_ID")
    private String userId;
    
    @Column(name = "ACTION_TYPE")
    private String actionType;
    
    @Column(name = "ACTION_TARGET")
    private String actionTarget;
    
    @Column(name = "ACTION_URL")
    private String actionUrl;
    
    @Column(name = "ACTION_DATA")
    @Lob
    private String actionData;
    
    @Column(name = "IP_ADDRESS")
    private String ipAddress;
    
    @Column(name = "USER_AGENT")
    private String userAgent;
    
    @Column(name = "ACTION_TIMESTAMP")
    private LocalDateTime actionTimestamp = LocalDateTime.now();
    
    @Column(name = "SESSION_ID")
    private String sessionId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionTarget() {
        return actionTarget;
    }

    public void setActionTarget(String actionTarget) {
        this.actionTarget = actionTarget;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getActionData() {
        return actionData;
    }

    public void setActionData(String actionData) {
        this.actionData = actionData;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getActionTimestamp() {
        return actionTimestamp;
    }

    public void setActionTimestamp(LocalDateTime actionTimestamp) {
        this.actionTimestamp = actionTimestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
} 