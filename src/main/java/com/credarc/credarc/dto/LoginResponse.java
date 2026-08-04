package com.credarc.credarc.dto;

import java.util.List;
import java.util.UUID;

public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private UUID userId;
    private String userName;
    private String email;
    private List<AccountSummary> accounts;
    private String message;


    /** Getters **/

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getMessage() {
        return message;
    }

    public List<AccountSummary> getAccounts() {
        return accounts;
    }

    /** Setters **/

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setAccounts(List<AccountSummary> accounts) {
        this.accounts = accounts;
    }
}
