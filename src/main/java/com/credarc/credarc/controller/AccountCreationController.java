package com.credarc.credarc.controller;

import com.credarc.credarc.dto.AccountCreationRequest;
import com.credarc.credarc.dto.AccountCreationResponse;
import com.credarc.credarc.service.AccountCreationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountCreationController {

    private final AccountCreationService accountCreationService;

    public AccountCreationController(AccountCreationService accountCreationService) {
        this.accountCreationService = accountCreationService;
    }


    @PostMapping("/new/account")
    public AccountCreationResponse createAccount(@Valid @RequestBody AccountCreationRequest request){
        return accountCreationService.processAccountDetails(request);
    }
}
