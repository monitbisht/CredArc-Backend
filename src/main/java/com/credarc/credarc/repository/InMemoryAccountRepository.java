package com.credarc.credarc.repository;

import com.credarc.credarc.entity.Account;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAccountRepository implements AccountRepository{

    private final ConcurrentHashMap<UUID,Account> storageMap = new ConcurrentHashMap<>();

    @Override
    public Account save(Account account) {

        // Mimicking the DB auto generation
        if (account.getAccountId() == null) {
            account.setAccountId(UUID.randomUUID());
        }

        storageMap.put(account.getAccountId(),account);

        return account;
    }

    @Override
    public Optional<Account> findById(UUID id) {

        return Optional.ofNullable(storageMap.get(id));
    }
}
