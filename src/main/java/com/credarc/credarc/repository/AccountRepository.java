package com.credarc.credarc.repository;

import com.credarc.credarc.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account,UUID> {

}
