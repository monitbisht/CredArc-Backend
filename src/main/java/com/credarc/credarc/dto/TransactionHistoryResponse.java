package com.credarc.credarc.dto;

import com.credarc.credarc.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class TransactionHistoryResponse {

    private UUID transactionId;
    private UUID accountId;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private Instant timestamp;


    /** Getters **/

    public UUID getTransactionId() {
        return transactionId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    /** Setters **/

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
