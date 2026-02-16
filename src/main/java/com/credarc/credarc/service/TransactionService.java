package com.credarc.credarc.service;

import com.credarc.credarc.entity.Account;
import com.credarc.credarc.entity.Transaction;
import com.credarc.credarc.entity.TransactionType;
import com.credarc.credarc.exception.AccountNotFoundException;
import com.credarc.credarc.exception.InsufficientBalanceException;
import com.credarc.credarc.repository.AccountRepository;
import com.credarc.credarc.repository.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void debit(UUID accountId, BigDecimal amount){

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

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


        transactionRepository.save(tx);
    }

    @Transactional
    public void credit(UUID accountId, BigDecimal amount){

        if(amount == null || amount.compareTo(BigDecimal.ZERO) <=0){
            throw new IllegalArgumentException("Amount must be positive.");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(()->
                    new AccountNotFoundException(accountId));

        account.setBalance(account.getBalance().add(amount));

        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setType(TransactionType.CREDIT);
        tx.setAmount(amount);
        tx.setDescription("Credit operation");

        transactionRepository.save(tx);
    }

    @Transactional
    public void transfer(UUID fromId,UUID toId , BigDecimal amount){

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

        transactionRepository.save(debitTx);
        transactionRepository.save(creditTx);
    }
}
