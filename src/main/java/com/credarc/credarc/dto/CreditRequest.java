package com.credarc.credarc.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;


import java.math.BigDecimal;
import java.util.UUID;

public class CreditRequest {

    @NotNull
    private UUID accountId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;


    public UUID getAccountId() {
        return accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
