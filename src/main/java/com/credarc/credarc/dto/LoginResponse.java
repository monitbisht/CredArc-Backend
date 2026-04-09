package com.credarc.credarc.dto;

import java.util.UUID;

public class LoginResponse {

    private String token;
    private UUID userId;
    private String userName;
    private String email;
    private UUID accountId;
    private String accountNumber;
    private String message;


    /** Getters **/

    public String getToken() {
        return token;
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

    public UUID getAccountId() {
        return accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getMessage() {
        return message;
    }

    /** Setters **/

    public void setToken(String token) {
        this.token = token;
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

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
