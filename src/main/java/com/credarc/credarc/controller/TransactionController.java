package com.credarc.credarc.controller;

import com.credarc.credarc.dto.*;
import com.credarc.credarc.security.CustomUserDetails;
import com.credarc.credarc.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")

public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    private UUID getRequestingUserId() {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return currentUser.getUserId();
    }

    @PostMapping("/debit")
    public TransactionResponse debit(@Valid @RequestBody DebitRequest debitRequest) {
        return transactionService.debit(
                debitRequest.getAccountId(),
                debitRequest.getAmount(),
                getRequestingUserId()
        );
    }

    @PostMapping("/transfer")
    public TransactionResponse transfer(@Valid @RequestBody TransferRequest transferRequest) {
        return transactionService.transfer(
                transferRequest.getFromAccountId(),
                transferRequest.getToAccountId(),
                transferRequest.getAmount(),
                getRequestingUserId()
        );
    }

    @GetMapping("/{accountId}")
    public Page<TransactionHistoryResponse> findAllByAccountId(@PathVariable UUID accountId,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size) {

        return transactionService.getTransactionHistory( accountId, page , size , getRequestingUserId() );

    }

    @Operation(
            summary = "Credit an account (dev/admin use)",
            description = "Adds funds to any account by ID. Intended for seeding test data " +
                    "or admin-initiated deposits — not a self-service user action. " +
                    "No ownership check is enforced; access control (RBAC) is planned " +
                    "but not yet implemented."
    )
    @PostMapping("/credit")
    public TransactionResponse credit( @Valid @RequestBody CreditRequest creditRequest){

        return transactionService.credit(creditRequest.getAccountId(), creditRequest.getAmount());
    }
}
