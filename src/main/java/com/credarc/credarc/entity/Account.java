package com.credarc.credarc.entity;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Account {

    private UUID accountId;

    private UUID customerId;

    private String accountNumber;

    private AccountStatus status = AccountStatus.ACTIVE;

    private Instant createdAt = Instant.now();

    private BigDecimal balance =  BigDecimal.ZERO;
    /* BigDecimal.ZERO points to one static object shared by everyone
    instead of creating new object on the heap every time an Account is created. */



    /** Getters **/

    public UUID getAccountId() { return accountId; }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public AccountStatus getStatus() { return status; }

    public Instant getCreatedAt() { return createdAt; }

    public BigDecimal getBalance() {
        return balance;
    }




    /** Setters **/

    public void setStatus(AccountStatus status) { this.status = status; }

    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
