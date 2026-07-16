package com.credarc.credarc.service;

import com.credarc.credarc.dto.TransactionResponse;
import com.credarc.credarc.entity.Account;
import com.credarc.credarc.entity.Transaction;
import com.credarc.credarc.entity.TransactionType;
import com.credarc.credarc.entity.User;
import com.credarc.credarc.exception.InsufficientBalanceException;
import com.credarc.credarc.repository.AccountRepository;
import com.credarc.credarc.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    TransactionService service;

    @Mock
    AccountService accountService;


    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;

    private Account mockAccount;
    private User dummyUser;
    private Transaction mockTransaction;


    @Test
    void debit() {

        dummyUser = new User();


        mockAccount = new Account();
        mockAccount.setBalance(new BigDecimal("1000"));
        mockAccount.setUser(dummyUser);
        ReflectionTestUtils.setField(mockAccount, "accountId", UUID.randomUUID());

        mockTransaction = new Transaction();
        mockTransaction.setAmount(new BigDecimal("100"));
        mockTransaction.setDescription("Debit operation");
        mockTransaction.setType(TransactionType.DEBIT);
        mockTransaction.setAccount(mockAccount);

        doNothing().when(accountService).verifyOwnership(any(UUID.class), any(UUID.class));
        when(accountRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);


        BigDecimal amount = new BigDecimal("100");
        TransactionResponse result = service.debit(UUID.randomUUID(),amount ,UUID.randomUUID());

        assertNotNull(result);
        assertEquals(TransactionType.DEBIT, result.getType());
        assertEquals(new BigDecimal("100"), result.getAmount());

        assertThrows(InsufficientBalanceException.class,
                () -> service.debit(UUID.randomUUID(), new BigDecimal("5000"), UUID.randomUUID()));

    }

    @Test
    void credit() {

        dummyUser = new User();


        mockAccount = new Account();
        mockAccount.setBalance(new BigDecimal("1000"));
        mockAccount.setUser(dummyUser);
        ReflectionTestUtils.setField(mockAccount, "accountId", UUID.randomUUID());

        mockTransaction = new Transaction();
        mockTransaction.setAmount(new BigDecimal("100"));
        mockTransaction.setDescription("Credit operation");
        mockTransaction.setType(TransactionType.CREDIT);
        mockTransaction.setAccount(mockAccount);

        when(accountRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

        BigDecimal amount = new BigDecimal("100");

        TransactionResponse result = service.debit(UUID.randomUUID(),amount ,UUID.randomUUID());

        assertNotNull(result);
        assertEquals(TransactionType.CREDIT, result.getType());
        assertEquals(new BigDecimal("100"), result.getAmount());

    }

    @Test
    void transfer() {
    }

    @Test
    void getTransactionHistory() {
    }
}