package com.credarc.credarc.service;

import com.credarc.credarc.dto.AccountResponse;
import com.credarc.credarc.dto.AccountSummary;
import com.credarc.credarc.entity.Account;
import com.credarc.credarc.entity.AccountStatus;
import com.credarc.credarc.entity.User;
import com.credarc.credarc.exception.AccountNotFoundException;
import com.credarc.credarc.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
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
        List<Account> accounts = accountRepository.findAllByUser(user);
        if (accounts.isEmpty()) {
            return createDefaultAccount(user);
        }
        return accounts.get(0);
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
    public AccountResponse createNewAccount(UUID userId) {

        User user = userService.getUserByUserId(userId);

        int accountCount = accountRepository.countByUser(user);
        Account savedAccount;

        if (accountCount >= 2) {
            throw new IllegalStateException("Maximum limit of 2 accounts reached.");
        }
        else {
            Account account = new Account();
            account.setUser(user);
            account.setAccountNumber(accNumberGeneration());
            account.setStatus(AccountStatus.ACTIVE);
            account.setBalance(BigDecimal.ZERO);
            savedAccount = accountRepository.save(account);
        }

        AccountResponse response = new AccountResponse();

        response.setAccountId(savedAccount.getAccountId());
        response.setAccountNumber(savedAccount.getAccountNumber());
        response.setBalance(savedAccount.getBalance());
        response.setStatus(AccountStatus.ACTIVE);
        response.setUserId(userId);
        response.setCreatedAt(savedAccount.getCreatedAt());
        return response;
    }


    @Cacheable(value = "userAccounts" , key = "#userId")
    public List<AccountResponse> getAllAccounts(UUID userId) {
        User user = userService.getUserByUserId(userId);
        List<Account> accounts = accountRepository.findAllByUser(user);

        return accounts.stream()
                .sorted(Comparator.comparing(Account::getCreatedAt))
                .map(account -> {
            AccountResponse response = new AccountResponse();
            response.setAccountId(account.getAccountId());
            response.setUserId(userId);
            response.setAccountNumber(account.getAccountNumber());
            response.setStatus(account.getStatus());
            response.setBalance(account.getBalance());
            response.setCreatedAt(account.getCreatedAt());
            return response;
        }).toList();
    }

    public List<AccountSummary> getAllAccountSummaries(User user) {
        return accountRepository.findAllByUser(user)
                .stream()
                .sorted(Comparator.comparing(Account::getCreatedAt))
                .map(acc -> {
                    AccountSummary summary = new AccountSummary();
                    summary.setAccountId(acc.getAccountId());
                    summary.setAccountNumber(acc.getAccountNumber());
                    summary.setBalance(acc.getBalance());
                    return summary;
                }).toList();
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

    public void verifyOwnership(UUID accountId, UUID requestingUserId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if (!account.getUser().getUserId().equals(requestingUserId)) {
            throw new AccessDeniedException("You do not have access to this account.");
        }
    }
}

