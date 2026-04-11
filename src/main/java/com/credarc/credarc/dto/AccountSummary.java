package com.credarc.credarc.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountSummary {

    private UUID accountId;
    private String accountNumber;
    private BigDecimal balance;

    /** Getters **/

    public UUID getAccountId() {
        return accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    /** Setters **/

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
