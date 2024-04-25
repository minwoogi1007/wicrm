package com.wio.crm.dto;

public class PasswordChangeDto {
    private String currentPassword;
    private String newPassword;

    // Constructors, getters, and setters
    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
