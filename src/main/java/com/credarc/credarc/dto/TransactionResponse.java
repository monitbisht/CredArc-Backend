package com.credarc.credarc.dto;

import com.credarc.credarc.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class TransactionResponse {

    private UUID transactionId;
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
    private TransactionType type;
    private BigDecimal updatedBalance;
    private Instant timestamp;

    /** Getters **/

    public UUID getTransactionId() {
        return transactionId;
    }

    public UUID getFromAccountId() {
        return fromAccountId;
    }

    public UUID getToAccountId() {
        return toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getUpdatedBalance() {
        return updatedBalance;
    }

    public Instant getTimestamp() {
        return timestamp;
    }


    /** Setters **/

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public void setFromAccountId(UUID fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public void setToAccountId(UUID toAccountId) {
        this.toAccountId = toAccountId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public void setUpdatedBalance(BigDecimal updatedBalance) {
        this.updatedBalance = updatedBalance;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
