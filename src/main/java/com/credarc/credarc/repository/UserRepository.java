package com.credarc.credarc.repository;

import com.credarc.credarc.entity.User;

import java.util.Optional;

public interface UserRepository  {

    User save(User user);

    Optional<User> findByEmail(String email);
}
