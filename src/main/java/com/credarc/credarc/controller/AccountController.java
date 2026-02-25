package com.credarc.credarc.controller;

import com.credarc.credarc.dto.AccountCreationRequest;
import com.credarc.credarc.dto.AccountResponse;
import com.credarc.credarc.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }


    @PostMapping("/new/account")
    public AccountResponse createAccount(@Valid @RequestBody AccountCreationRequest request){
        return accountService.createAccount(request);
    }

    @GetMapping("/accounts/{id}")
    public AccountResponse getAccount(@PathVariable UUID id){
        return accountService.getAccount(id);
    }
}
