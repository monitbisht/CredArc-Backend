package com.credarc.credarc.service;

import com.credarc.credarc.dto.TransactionHistoryResponse;
import com.credarc.credarc.dto.TransactionResponse;
import com.credarc.credarc.entity.Account;
import com.credarc.credarc.entity.Transaction;
import com.credarc.credarc.entity.TransactionType;
import com.credarc.credarc.exception.AccountNotFoundException;
import com.credarc.credarc.exception.InsufficientBalanceException;
import com.credarc.credarc.redis.CacheEvictionService;
import com.credarc.credarc.redis.RateLimiterService;
import com.credarc.credarc.repository.AccountRepository;
import com.credarc.credarc.repository.TransactionRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final CacheEvictionService cacheEvictionService;

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository, AccountService accountService, CacheEvictionService cacheEvictionService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.cacheEvictionService = cacheEvictionService;
    }

    @Transactional
    public TransactionResponse debit(UUID accountId, BigDecimal amount, UUID requestingUserId) {
        accountService.verifyOwnership(accountId, requestingUserId);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be positive");


        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        TransactionResponse transactionResponse = new TransactionResponse();

        BigDecimal bal = account.getBalance();

        if (bal.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }

        account.setBalance(bal.subtract(amount));

        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setType(TransactionType.DEBIT);
        tx.setAmount(amount);
        tx.setDescription("Debit operation");


        Transaction savedTransaction = transactionRepository.save(tx);

        transactionResponse.setTransactionId(savedTransaction.getTransactionId());
        transactionResponse.setFromAccountId(savedTransaction.getAccount().getAccountId());
        transactionResponse.setAmount(savedTransaction.getAmount());
        transactionResponse.setType(savedTransaction.getType());
        transactionResponse.setTimestamp(savedTransaction.getCreatedAt());
        transactionResponse.setUpdatedBalance(savedTransaction.getAccount().getBalance());

        cacheEvictionService.evictAccountDetailsCache(account.getUser().getUserId());

        return transactionResponse;
    }

    @Transactional
    public TransactionResponse transfer(UUID fromId,UUID toId , BigDecimal amount , UUID requestingUserId) {
        accountService.verifyOwnership(fromId, requestingUserId);
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Cannot transfer to same account.");
        }


        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Amount must be positive.");
        }

        UUID firstLock = fromId.compareTo(toId) < 0 ?fromId : toId;
        UUID secondLock = fromId.compareTo(toId) < 0 ?toId : fromId;


        Account first = accountRepository.findById(firstLock)
                .orElseThrow(()->
                        new AccountNotFoundException(firstLock));

        Account second = accountRepository.findById(secondLock)
                .orElseThrow(()->
                        new AccountNotFoundException(secondLock));

        Account fromAccount = first.getAccountId().equals(fromId) ? first : second;
        Account toAccount = first.getAccountId().equals(fromId) ? second : first;

        TransactionResponse transactionResponse = new TransactionResponse();


        if (fromAccount.getBalance().compareTo(amount) < 0){
            throw new InsufficientBalanceException();
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        Transaction debitTx = new Transaction();
        debitTx.setAccount(fromAccount);
        debitTx.setType(TransactionType.DEBIT);
        debitTx.setAmount(amount);
        debitTx.setDescription("Transfer to " + toId);

        Transaction creditTx = new Transaction();
        creditTx.setAccount(toAccount);
        creditTx.setType(TransactionType.CREDIT);
        creditTx.setAmount(amount);
        creditTx.setDescription("Transfer from " + fromId);

        Transaction savedTransaction = transactionRepository.save(debitTx);
        Transaction secondSavedTransaction = transactionRepository.save(creditTx);
        transactionResponse.setTransactionId(savedTransaction.getTransactionId());
        transactionResponse.setFromAccountId(savedTransaction.getAccount().getAccountId());
        transactionResponse.setToAccountId(secondSavedTransaction.getAccount().getAccountId());
        transactionResponse.setAmount(savedTransaction.getAmount());
        transactionResponse.setType(TransactionType.TRANSFER);
        transactionResponse.setTimestamp(savedTransaction.getCreatedAt());
        transactionResponse.setUpdatedBalance(savedTransaction.getAccount().getBalance());

        cacheEvictionService.evictAccountDetailsCache(fromAccount.getUser().getUserId());
        cacheEvictionService.evictAccountDetailsCache(toAccount.getUser().getUserId());

        return transactionResponse;
    }

    public Page<TransactionHistoryResponse> getTransactionHistory(UUID accountId, int page, int size, UUID requestingUserId) {
        accountService.verifyOwnership(accountId, requestingUserId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionRepository.findByAccount_AccountIdOrderByCreatedAtDesc(accountId, pageable);

        return transactions.map(tx -> {
            TransactionHistoryResponse transactionResponse = new TransactionHistoryResponse();
           transactionResponse.setTransactionId(tx.getTransactionId());
           transactionResponse.setAccountId(tx.getAccount().getAccountId());
           transactionResponse.setType(tx.getType());
           transactionResponse.setAmount(tx.getAmount());
           transactionResponse.setDescription(tx.getDescription());
           transactionResponse.setTimestamp(tx.getCreatedAt());
            return transactionResponse;
        });
    }

    /**
     * Credits funds to an account. Currently unrestricted (no ownership check) —
     * intended as an admin/system-initiated action (e.g. external deposit) or
     * dev/testing utility to seed account balances. Not exposed to regular
     * users in a production context; requires RBAC before real deployment.
     */

    @Transactional
    public TransactionResponse credit(UUID accountId, BigDecimal amount){

        if(amount == null || amount.compareTo(BigDecimal.ZERO) <=0){
            throw new IllegalArgumentException("Amount must be positive.");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(()->
                        new AccountNotFoundException(accountId));

        TransactionResponse transactionResponse = new TransactionResponse();


        account.setBalance(account.getBalance().add(amount));

        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setType(TransactionType.CREDIT);
        tx.setAmount(amount);
        tx.setDescription("Credit operation");

        Transaction savedTransaction = transactionRepository.save(tx);

        transactionResponse.setTransactionId(savedTransaction.getTransactionId());
        transactionResponse.setToAccountId(savedTransaction.getAccount().getAccountId());
        transactionResponse.setAmount(savedTransaction.getAmount());
        transactionResponse.setType(savedTransaction.getType());
        transactionResponse.setTimestamp(savedTransaction.getCreatedAt());
        transactionResponse.setUpdatedBalance(savedTransaction.getAccount().getBalance());

        cacheEvictionService.evictAccountDetailsCache(account.getUser().getUserId());

        return transactionResponse;
    }
}
