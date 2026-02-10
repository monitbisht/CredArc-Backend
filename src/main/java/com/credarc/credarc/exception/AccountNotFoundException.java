package com.credarc.credarc.exception;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException{

    private final UUID accountId;

    public AccountNotFoundException(UUID accountId){
        super("Account with id " + accountId + " not found.");
        this.accountId = accountId;
    }

    public UUID getAccountId() {
        return accountId;
    }
}
