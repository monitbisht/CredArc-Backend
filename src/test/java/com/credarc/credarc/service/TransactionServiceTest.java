package com.credarc.credarc.service;

import com.credarc.credarc.dto.TransactionHistoryResponse;
import com.credarc.credarc.dto.TransactionResponse;
import com.credarc.credarc.entity.Account;
import com.credarc.credarc.entity.Transaction;
import com.credarc.credarc.entity.TransactionType;
import com.credarc.credarc.entity.User;
import com.credarc.credarc.exception.AccountNotFoundException;
import com.credarc.credarc.exception.InsufficientBalanceException;
import com.credarc.credarc.redis.CacheEvictionService;
import com.credarc.credarc.redis.RateLimiterService;
import com.credarc.credarc.repository.AccountRepository;
import com.credarc.credarc.repository.TransactionRepository;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    TransactionService service;

    @Mock
    AccountService accountService;

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private CacheEvictionService cacheEvictionService;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;

    private Account mockAccount;
    private User dummyUser;
    private Transaction mockTransaction;


    @Test
    void debit_success() {
        dummyUser = new User();

        mockAccount = new Account();
        mockAccount.setBalance(new BigDecimal("1000"));
        mockAccount.setUser(dummyUser);
        ReflectionTestUtils.setField(mockAccount, "accountId", UUID.randomUUID());

        doNothing().when(accountService).verifyOwnership(any(UUID.class), any(UUID.class));
        when(accountRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse result = service.debit(UUID.randomUUID(), new BigDecimal("100"), UUID.randomUUID());

        assertNotNull(result);
        assertEquals(TransactionType.DEBIT, result.getType());
        assertEquals(new BigDecimal("100"), result.getAmount());
    }

    @Test
    void debit_throwsException_whenInsufficientBalance() {
        dummyUser = new User();
        mockAccount = new Account();
        mockAccount.setBalance(new BigDecimal("1000"));
        mockAccount.setUser(dummyUser);
        ReflectionTestUtils.setField(mockAccount, "accountId", UUID.randomUUID());

        doNothing().when(accountService).verifyOwnership(any(UUID.class), any(UUID.class));
        when(accountRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockAccount));

        assertThrows(InsufficientBalanceException.class,
                () -> service.debit(UUID.randomUUID(), new BigDecimal("5000"), UUID.randomUUID()));
    }

    @Test
    void credit_success() {

        dummyUser = new User();


        mockAccount = new Account();
        mockAccount.setBalance(new BigDecimal("1000"));
        mockAccount.setUser(dummyUser);
        ReflectionTestUtils.setField(mockAccount, "accountId", UUID.randomUUID());


        when(accountRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse result = service.credit(UUID.randomUUID(),new BigDecimal("100"));

        assertNotNull(result);
        assertEquals(TransactionType.CREDIT, result.getType());
        assertEquals(new BigDecimal("100"), result.getAmount());
    }

    @Test
    void credit_throwsException_whenAmountIsNegative() {

        assertThrows(IllegalArgumentException.class,
                () -> service.credit(UUID.randomUUID(),new BigDecimal("-1")));
    }


   @Test
    void transfer_throwsException_whenSameAccount(){

        doNothing().when(accountService).verifyOwnership(any(UUID.class), any(UUID.class));

        UUID accountId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                ()-> service.transfer(accountId,accountId,new BigDecimal("100"),UUID.randomUUID()));
   }

   @Test
    void transfer_throwsException_whenAmountIsNegative(){
       doNothing().when(accountService).verifyOwnership(any(UUID.class), any(UUID.class));

       UUID accountId = UUID.randomUUID();

       assertThrows(IllegalArgumentException.class,
               ()-> service.transfer(accountId,UUID.randomUUID(),new BigDecimal("-1"),UUID.randomUUID()));
   }

    @Test
    void transfer_throwsException_whenFromAccountNotFound() {
        UUID fromId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID toId   = UUID.fromString("00000000-0000-0000-0000-000000000002");
        // fromId < toId, so firstLock = fromId — this lookup fails first, method should throw before reaching toId

        doNothing().when(accountService).verifyOwnership(any(UUID.class), any(UUID.class));
        when(accountRepository.findById(fromId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> service.transfer(fromId, toId, new BigDecimal("100"), UUID.randomUUID()));
    }

    @Test
    void transfer_throwsException_whenToAccountNotFound() {
        dummyUser = new User();

        mockAccount = new Account();
        mockAccount.setBalance(new BigDecimal("1000"));
        mockAccount.setUser(dummyUser);
        ReflectionTestUtils.setField(dummyUser, "userId", UUID.randomUUID());

        doNothing().when(accountService).verifyOwnership(any(UUID.class), any(UUID.class));

        UUID fromId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID toId   = UUID.fromString("00000000-0000-0000-0000-000000000002");
        // fromId < toId lexicographically, so firstLock = fromId, secondLock = toId

        when(accountRepository.findById(fromId)).thenReturn(Optional.of(mockAccount));
        when(accountRepository.findById(toId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> service.transfer(fromId, toId, new BigDecimal("100"), mockAccount.getUser().getUserId()));
    }

    @Test
    void transfer_throwsException_whenInsufficientBalance() {
        dummyUser = new User();
        User newDummyUser = new User();
        ReflectionTestUtils.setField(dummyUser, "userId", UUID.randomUUID());

        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();

        Account mockFromAccount = new Account();
        mockFromAccount.setBalance(new BigDecimal("50"));
        mockFromAccount.setUser(dummyUser);
        ReflectionTestUtils.setField(mockFromAccount, "accountId", fromAccountId);

        Account mockToAccount = new Account();
        mockToAccount.setBalance(new BigDecimal("1000"));
        mockToAccount.setUser(newDummyUser);
        ReflectionTestUtils.setField(mockToAccount, "accountId", toAccountId);

        doNothing().when(accountService).verifyOwnership(any(UUID.class), any(UUID.class));
        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(mockFromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(mockToAccount));

        assertThrows(InsufficientBalanceException.class,
                () -> service.transfer(fromAccountId, toAccountId, new BigDecimal("100"), dummyUser.getUserId()));
    }

    @Test
    void transfer_success(){

        dummyUser = new User();
        User newDummyUser = new User();

        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();

        Account mockFromAccount = new Account();
        mockFromAccount.setBalance(new BigDecimal("1000"));
        mockFromAccount.setUser(dummyUser);
        ReflectionTestUtils.setField(dummyUser, "userId",UUID.randomUUID());
        ReflectionTestUtils.setField(mockFromAccount, "accountId", fromAccountId);

        Account mockToAccount = new Account();
        mockToAccount.setBalance(new BigDecimal("1000"));
        mockToAccount.setUser(newDummyUser);
        ReflectionTestUtils.setField(mockToAccount, "accountId", toAccountId);

        doNothing().when(accountService).verifyOwnership(any(UUID.class), any(UUID.class));

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(mockFromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(mockToAccount));

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = service.transfer(fromAccountId,toAccountId,new BigDecimal("100"),mockFromAccount.getUser().getUserId());
        assertNotNull(response);
        assertEquals(TransactionType.TRANSFER, response.getType());
        assertEquals(new BigDecimal("100"), response.getAmount());
        assertEquals(new BigDecimal("900"), mockFromAccount.getBalance());
        assertEquals(new BigDecimal("1100"), mockToAccount.getBalance());
        assertEquals(fromAccountId, response.getFromAccountId());
        assertEquals(toAccountId, response.getToAccountId());
    }
    @Test
    void getTransactionHistory_success() {
        UUID accountId = UUID.randomUUID();

        Account mockAccount = new Account();
        ReflectionTestUtils.setField(mockAccount, "accountId", accountId);

        Transaction tx = new Transaction();
        tx.setType(TransactionType.DEBIT);
        tx.setAmount(new BigDecimal("100"));
        tx.setDescription("Debit operation");
        tx.setAccount(mockAccount);
        ReflectionTestUtils.setField(tx, "transactionId", UUID.randomUUID());

        Page<Transaction> mockPage = new PageImpl<>(List.of(tx));

        doNothing().when(accountService).verifyOwnership(any(UUID.class), any(UUID.class));
        when(transactionRepository.findByAccount_AccountIdOrderByCreatedAtDesc(eq(accountId), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<TransactionHistoryResponse> result = service.getTransactionHistory(accountId, 0, 10, UUID.randomUUID());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        TransactionHistoryResponse responseItem = result.getContent().get(0);
        assertEquals(tx.getTransactionId(), responseItem.getTransactionId());
        assertEquals(tx.getType(), responseItem.getType());
        assertEquals(tx.getAmount(), responseItem.getAmount());
        assertEquals(tx.getDescription(), responseItem.getDescription());
        assertEquals(mockAccount.getAccountId(), responseItem.getAccountId());
    }

    @Test
    void getTransactionHistory_returnsEmptyPage_whenNoTransactions() {
        UUID accountId = UUID.randomUUID();

        Page<Transaction> emptyPage = new PageImpl<>(List.of());

        doNothing().when(accountService).verifyOwnership(any(UUID.class), any(UUID.class));
        when(transactionRepository.findByAccount_AccountIdOrderByCreatedAtDesc(eq(accountId), any(Pageable.class)))
                .thenReturn(emptyPage);

        Page<TransactionHistoryResponse> result = service.getTransactionHistory(accountId, 0, 10, UUID.randomUUID());

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getTransactionHistory_callsVerifyOwnership() {
        UUID accountId = UUID.randomUUID();
        UUID requestingUserId = UUID.randomUUID();

        doNothing().when(accountService).verifyOwnership(any(UUID.class), any(UUID.class));
        when(transactionRepository.findByAccount_AccountIdOrderByCreatedAtDesc(eq(accountId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.getTransactionHistory(accountId, 0, 10, requestingUserId);

        verify(accountService).verifyOwnership(accountId, requestingUserId);
    }

    @Test
    void getTransactionHistory_passesCorrectPageableToRepository() {
        UUID accountId = UUID.randomUUID();

        doNothing().when(accountService).verifyOwnership(any(UUID.class), any(UUID.class));
        when(transactionRepository.findByAccount_AccountIdOrderByCreatedAtDesc(eq(accountId), eq(PageRequest.of(2, 5))))
                .thenReturn(new PageImpl<>(List.of()));

        Page<TransactionHistoryResponse> result = service.getTransactionHistory(accountId, 2, 5, UUID.randomUUID());

        assertNotNull(result);
    }
}