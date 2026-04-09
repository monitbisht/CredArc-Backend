package com.credarc.credarc.repository;

import com.credarc.credarc.entity.Account;
import com.credarc.credarc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account,UUID> {

    Optional<Account> findByUser(User user);
}
