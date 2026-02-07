package com.credarc.credarc.dto;

import com.credarc.credarc.entity.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class AccountCreationResponse {
    private String message;
    private UUID accountId;
    private UUID customerId;
    private String accountNumber;
    private AccountStatus status ;
    private Instant createdAt ;
    private BigDecimal balance ;


    /** Getters **/

    public String getMessage() {
        return message;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    /** Setters **/

    public void setMessage(String message) {
        this.message = message;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}