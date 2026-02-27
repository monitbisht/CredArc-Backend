package com.credarc.credarc.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

     public String hashPassword(String rawPassword) {

        return passwordEncoder.encode(rawPassword);
    }

    public boolean matchPassword(String rawPassword , String hashedPassword){
         return passwordEncoder.matches(rawPassword,hashedPassword);
    }
}
