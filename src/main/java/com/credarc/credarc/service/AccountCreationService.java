package com.credarc.credarc.service;

import com.credarc.credarc.dto.AccountCreationRequest;
import com.credarc.credarc.dto.AccountCreationResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountCreationService {
    private UUID customerId;

    public AccountCreationResponse processAccountDetails(AccountCreationRequest request) {
        AccountCreationResponse response = new AccountCreationResponse();
        response.setConfirmationMessage("Account Created Successfully!!");

        return response;
    }


}
