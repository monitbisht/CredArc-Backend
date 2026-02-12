package com.credarc.credarc.service;


import com.credarc.credarc.dto.AccountCreationRequest;
import com.credarc.credarc.entity.User;
import com.credarc.credarc.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findOrCreateUser(AccountCreationRequest request){
        return userRepository.findByEmail(request.getEmail())
                .orElseGet(()->{
                    User newUser = new User();
                    newUser.setName(request.getName());
                    newUser.setEmail(request.getEmail());
                    newUser.setMobile(request.getMobile());
                    newUser.setCreatedAt(Instant.now());

                   return userRepository.save(newUser);

                });
    }

}
