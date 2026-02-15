package com.credarc.credarc.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionService {

    @Transactional
    public void debit(UUID accountId, BigDecimal amount){

    }

    @Transactional
    public void credit(UUID accountId, BigDecimal amount){

    }

    @Transactional
    public void transfer(UUID fromId,UUID toId , BigDecimal amount){

    }
}
