package com.credarc.credarc.repository;

import com.credarc.credarc.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository implements UserRepository{

    private final ConcurrentHashMap<UUID,User> storage = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        if (user.getUserId() == null){
            user.setUserId(UUID.randomUUID());
        }

        storage.put(user.getUserId(),user);
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return storage.values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
