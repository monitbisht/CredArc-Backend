package com.credarc.credarc.controller;

import com.credarc.credarc.dto.AccountResponse;
import com.credarc.credarc.security.CustomUserDetails;
import com.credarc.credarc.service.AccountService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }


    @PostMapping("/new")
    public AccountResponse openNewAccount(){
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return accountService.createNewAccount(currentUser.getUserId());
    }


    @GetMapping("/all")
    public List<AccountResponse> getAllAccounts() {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return accountService.getAllAccounts(currentUser.getUserId());
    }
}
