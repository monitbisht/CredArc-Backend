package com.credarc.credarc.repository;

import com.credarc.credarc.entity.Account;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

     Account save(Account account);

     Optional<Account> findById(UUID id);
}
