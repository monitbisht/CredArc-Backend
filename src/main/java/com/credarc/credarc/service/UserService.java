package com.credarc.credarc.service;



import com.credarc.credarc.dto.SignupRequest;
import com.credarc.credarc.entity.User;
import com.credarc.credarc.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public boolean isEmailRegistered(String email){
        return userRepository.existsByEmail(email);
    }

    public boolean isMobileRegistered(String mobile){
        return userRepository.existsByMobile(mobile);
    }


    public User createUser(SignupRequest request , String hashedPassword){
        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setMobile(request.getMobile());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(hashedPassword);
        newUser.setCreatedAt(Instant.now());

        return userRepository.save(newUser);
    }

    public User getUserByEmail(String email){

        return userRepository.findByEmail(email)
                .orElseThrow(()->{
                        log.warn("Login failed: no user found for email: {}", email);
                        return new UsernameNotFoundException("User not found with email: " + email);
    });
    }

    public User getUserByUserId(UUID userID){

        return userRepository.findByUserId(userID)
                .orElseThrow(()->
                        new UsernameNotFoundException("User not found with id: " + userID));
    }
}
