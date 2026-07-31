package com.credarc.credarc.service;

import com.credarc.credarc.dto.AccountResponse;
import com.credarc.credarc.dto.AccountSummary;
import com.credarc.credarc.entity.Account;
import com.credarc.credarc.entity.AccountStatus;
import com.credarc.credarc.entity.User;
import com.credarc.credarc.exception.AccountNotFoundException;
import com.credarc.credarc.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserService userService;


    // defaultAccount()

    @Test
    void defaultAccount_returnsExistingAccount_whenAccountsExist(){
        User  mockUser = new User();
        Account mockAccount = new Account();
        mockAccount.setUser(mockUser);
        mockAccount.setBalance(new BigDecimal("1000"));
        mockAccount.setAccountNumber("00000000000000");
        mockAccount.setStatus(AccountStatus.ACTIVE);


        when(accountRepository.findAllByUser(eq(mockUser))).thenReturn(List.of(mockAccount));

        Account defaultAccount = accountService.defaultAccount(mockUser);

        assertEquals(mockAccount, defaultAccount);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void defaultAccount_createsNewAccount_whenNoAccountsExist(){
        User mockUser = new User();
        ReflectionTestUtils.setField(mockUser, "userId", UUID.randomUUID());

        when(accountRepository.findAllByUser(mockUser)).thenReturn(List.of());
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.defaultAccount(mockUser);

        assertNotNull(result);
        assertEquals(mockUser, result.getUser());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertNotNull(result.getAccountNumber());
        assertEquals(14, result.getAccountNumber().length());

        verify(accountRepository).save(any(Account.class));
    }

    // createNewAccount()

    @Test
    void createNewAccount_throwsException_whenAccountLimitReached() {
        User mockUser = new User();
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(mockUser, "userId", userId);

        when(userService.getUserByUserId(userId)).thenReturn(mockUser);

        when(accountRepository.countByUser(mockUser)).thenReturn(2);

        assertThrows(IllegalStateException.class,
                () -> accountService.createNewAccount(mockUser.getUserId()));

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void createNewAccount_success(){

        User mockUser = new User();
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(mockUser, "userId", userId);

        when(userService.getUserByUserId(userId)).thenReturn(mockUser);
        when(accountRepository.countByUser(mockUser)).thenReturn(1);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response =  accountService.createNewAccount(userId);

        assertNotNull(response);
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
        assertEquals(BigDecimal.ZERO, response.getBalance());
        assertNotNull(response.getAccountNumber());
        assertEquals(14, response.getAccountNumber().length());
        assertEquals(userId, response.getUserId());

        verify(accountRepository).save(any(Account.class));

    }

    // getAllAccounts()

    @Test
    void getAllAccounts_returnsEmptyList_whenNoAccounts(){

        User mockUser = new User();
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(mockUser, "userId", userId);

        when(userService.getUserByUserId(userId)).thenReturn(mockUser);
        when(accountRepository.findAllByUser(eq(mockUser))).thenReturn(List.of());

        List<AccountResponse> accounts = accountService.getAllAccounts(userId);

        assertTrue(accounts.isEmpty());
    }

    @Test
    void getAllAccounts_success(){
        User mockUser = new User();
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(mockUser, "userId", userId);

        Account mockAccount = new Account();
        mockAccount.setUser(mockUser);
        mockAccount.setBalance(new BigDecimal("1000"));
        mockAccount.setAccountNumber("00000000000000");
        mockAccount.setStatus(AccountStatus.ACTIVE);

        when(userService.getUserByUserId(userId)).thenReturn(mockUser);
        when(accountRepository.findAllByUser(eq(mockUser))).thenReturn(List.of(mockAccount));

        List<AccountResponse> accounts = accountService.getAllAccounts(userId);

        assertEquals(1, accounts.size());

        AccountResponse response = accounts.get(0);

        assertEquals(mockAccount.getAccountId(), response.getAccountId());
        assertEquals(userId, response.getUserId());
        assertEquals(mockAccount.getAccountNumber(), response.getAccountNumber());
        assertEquals(mockAccount.getStatus(), response.getStatus());
        assertEquals(mockAccount.getBalance(), response.getBalance());

    }

    @Test
    void getAllAccounts_returnsSortedByCreatedAt() {
        User mockUser = new User();
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(mockUser, "userId", userId);

        Account newAccount = new Account();
        newAccount.setUser(mockUser);
        newAccount.setBalance(new BigDecimal("500"));
        newAccount.setAccountNumber("11111111111111");
        newAccount.setStatus(AccountStatus.ACTIVE);
        ReflectionTestUtils.setField(newAccount, "accountId", UUID.randomUUID());
        ReflectionTestUtils.setField(newAccount, "createdAt", Instant.now());

        Account olderAccount = new Account();
        olderAccount.setUser(mockUser);
        olderAccount.setBalance(new BigDecimal("1000"));
        olderAccount.setAccountNumber("00000000000000");
        olderAccount.setStatus(AccountStatus.ACTIVE);
        ReflectionTestUtils.setField(olderAccount, "accountId", UUID.randomUUID());
        ReflectionTestUtils.setField(olderAccount, "createdAt", Instant.now().minusSeconds(3600));

        // deliberately inserted out of order: new first, older second
        when(userService.getUserByUserId(userId)).thenReturn(mockUser);
        when(accountRepository.findAllByUser(mockUser)).thenReturn(List.of(newAccount, olderAccount));

        List<AccountResponse> accounts = accountService.getAllAccounts(userId);

        assertEquals(2, accounts.size());
        assertEquals(olderAccount.getAccountId(), accounts.get(0).getAccountId());
        assertEquals(newAccount.getAccountId(), accounts.get(1).getAccountId());
    }

    // getAllAccountSummaries()

    @Test
    void getAllAccountSummaries_returnsEmptyList_whenNoAccounts(){
        User mockUser = new User();
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(mockUser, "userId", userId);

        when(accountRepository.findAllByUser(mockUser)).thenReturn(List.of());

        List<AccountSummary> summaries = accountService.getAllAccountSummaries(mockUser);

        assertTrue(summaries.isEmpty());
    }

    @Test
    void getAllAccountSummaries_success(){

        User mockUser = new User();
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(mockUser, "userId", userId);

        Account mockAccount = new Account();
        mockAccount.setUser(mockUser);
        mockAccount.setBalance(new BigDecimal("1000"));
        mockAccount.setAccountNumber("00000000000000");
        mockAccount.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findAllByUser(mockUser)).thenReturn(List.of(mockAccount));

        List<AccountSummary> summaries = accountService.getAllAccountSummaries(mockUser);

        assertEquals(1, summaries.size());

        AccountSummary summary = summaries.get(0);
        assertEquals(mockAccount.getAccountId(), summary.getAccountId());
        assertEquals(mockAccount.getAccountNumber(), summary.getAccountNumber());
        assertEquals(mockAccount.getBalance(), summary.getBalance());
    }

    @Test
    void getAllAccountSummaries_returnsSortedByCreatedAt(){
        User mockUser = new User();
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(mockUser, "userId", userId);

        Account newAccount = new Account();
        newAccount.setUser(mockUser);
        newAccount.setBalance(new BigDecimal("500"));
        newAccount.setAccountNumber("11111111111111");
        newAccount.setStatus(AccountStatus.ACTIVE);
        ReflectionTestUtils.setField(newAccount, "accountId", UUID.randomUUID());
        ReflectionTestUtils.setField(newAccount, "createdAt", Instant.now());

        Account olderAccount = new Account();
        olderAccount.setUser(mockUser);
        olderAccount.setBalance(new BigDecimal("1000"));
        olderAccount.setAccountNumber("00000000000000");
        olderAccount.setStatus(AccountStatus.ACTIVE);
        ReflectionTestUtils.setField(olderAccount, "accountId", UUID.randomUUID());
        ReflectionTestUtils.setField(olderAccount, "createdAt", Instant.now().minusSeconds(3600));

        when(accountRepository.findAllByUser(mockUser)).thenReturn(List.of(newAccount, olderAccount));
        List<AccountSummary> summaries = accountService.getAllAccountSummaries(mockUser);
        assertEquals(2, summaries.size());
        assertEquals(olderAccount.getAccountId(), summaries.get(0).getAccountId());
        assertEquals(newAccount.getAccountId(), summaries.get(1).getAccountId());
    }

    // verifyOwnership()

    @Test
    void verifyOwnership_throwsException_whenUserDoesNotOwnAccount(){
       when(accountRepository.findById(any())).thenReturn(Optional.empty());

       assertThrows(AccountNotFoundException.class,
               ()-> accountService.verifyOwnership(UUID.randomUUID(),UUID.randomUUID()));

   }

   @Test
    void verifyOwnership_throwsException_whenUserNotFound(){
        User mockUser = new User();
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(mockUser, "userId", userId);

       User owner = new User();
       UUID ownerId = UUID.randomUUID();
       ReflectionTestUtils.setField(owner, "userId", ownerId);

        Account account = new Account();
        UUID accountId = UUID.randomUUID();
        account.setUser(owner);
        account.setStatus(AccountStatus.ACTIVE);
        account.setAccountNumber("0000000000000000000");
        account.setBalance(new BigDecimal("1000"));
        ReflectionTestUtils.setField(account, "accountId", accountId);

       when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

       assertThrows(AccessDeniedException.class,
               ()-> accountService.verifyOwnership(accountId,userId));
   }

    @Test
    void verifyOwnership_success_whenUserOwnsAccount(){
        User owner = new User();
        UUID ownerId = UUID.randomUUID();
        ReflectionTestUtils.setField(owner, "userId", ownerId);

        Account account = new Account();
        UUID accountId = UUID.randomUUID();
        account.setUser(owner);
        account.setStatus(AccountStatus.ACTIVE);
        account.setAccountNumber("0000000000000000000");
        account.setBalance(new BigDecimal("1000"));
        ReflectionTestUtils.setField(account, "accountId", accountId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertDoesNotThrow(() -> accountService.verifyOwnership(accountId, ownerId));

        verify(accountRepository).findById(accountId);
    }
}