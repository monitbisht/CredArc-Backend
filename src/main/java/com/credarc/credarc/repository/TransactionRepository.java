package com.credarc.credarc.repository;

import com.credarc.credarc.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository <Transaction, UUID> {

    Page<Transaction> findByAccount_AccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);
}

