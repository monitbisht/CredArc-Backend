package com.credarc.credarc.service;

import com.credarc.credarc.dto.AccountCreationRequest;
import com.credarc.credarc.dto.AccountResponse;
import com.credarc.credarc.entity.Account;
import com.credarc.credarc.entity.AccountStatus;
import com.credarc.credarc.entity.User;
import com.credarc.credarc.exception.AccountNotFoundException;
import com.credarc.credarc.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserService userService;

    public AccountService(AccountRepository accountRepository, UserService userService) {
        this.accountRepository = accountRepository;
        this.userService = userService;
    }

    public Account defaultAccount(User user) {

        Account account = accountRepository.findByUser(user)
                .orElseGet(()->createDefaultAccount(user));
        return account;
    }

    private Account createDefaultAccount(User user) {
        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(accNumberGeneration());
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    @Transactional
    public AccountResponse createAccount(AccountCreationRequest request) {

        // Account class's object
        Account account = new Account();

        // AccountCreationResponse class's object
        AccountResponse response = new AccountResponse();

        //Populating Account class fields
        /*account.setUser(user);*/
        account.setAccountNumber(accNumberGeneration());
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(BigDecimal.ZERO);


        // Calling repo method to save account details
        Account savedAccount = accountRepository.save(account);

        //Setting up response for client
        response.setAccountId(savedAccount.getAccountId());
        response.setUserId(savedAccount.getUser().getUserId());
        response.setAccountNumber(savedAccount.getAccountNumber());
        response.setStatus(savedAccount.getStatus());
        response.setBalance(savedAccount.getBalance());
        response.setCreatedAt(savedAccount.getCreatedAt());
        return response;
    }


    public AccountResponse getAccount(UUID id){

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        AccountResponse getResponse = new AccountResponse();

        getResponse.setAccountId(account.getAccountId());
        getResponse.setUserId(account.getUser().getUserId());
        getResponse.setAccountNumber(account.getAccountNumber());
        getResponse.setStatus(account.getStatus());
        getResponse.setBalance(account.getBalance());
        getResponse.setCreatedAt(account.getCreatedAt());

        return getResponse;
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

