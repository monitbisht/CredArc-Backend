package com.credarc.credarc.service;

import com.credarc.credarc.dto.AccountCreationRequest;
import com.credarc.credarc.dto.AccountCreationResponse;
import com.credarc.credarc.entity.Account;
import com.credarc.credarc.entity.AccountStatus;
import com.credarc.credarc.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@Service
public class AccountCreationService {

    private final AccountRepository accountRepository; // Interface!

    public AccountCreationService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


    public AccountCreationResponse createAccount(AccountCreationRequest request) {

        // Account class's object
        Account account = new Account();

        // AccountCreationResponse class's object
        AccountCreationResponse response = new AccountCreationResponse();

        //Populating Account class fields
        account.setCreatedAt(Instant.now());
        account.setCustomerId(UUID.randomUUID());
        account.setAccountNumber(accNumberGeneration());
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(BigDecimal.ZERO);

        // Calling repo method to save account details
        Account savedAccount = accountRepository.save(account);

        //Setting up response for client
        response.setMessage("Account Created Successfully!!" );
        response.setAccountId(savedAccount.getAccountId());
        response.setCustomerId(savedAccount.getCustomerId());
        response.setAccountNumber(savedAccount.getAccountNumber());
        response.setStatus(savedAccount.getStatus());
        response.setBalance(savedAccount.getBalance());
        response.setCreatedAt(savedAccount.getCreatedAt());
        return response;
    }



    /** Helper methods **/

    private String accNumberGeneration() {
        int i = 0;

        Random random = new Random();
        StringBuilder builder = new StringBuilder();

        while (i < 14) {
            builder.append(random.nextInt(1, 10));
            i++;
        }


        return builder.toString();
    }

}

