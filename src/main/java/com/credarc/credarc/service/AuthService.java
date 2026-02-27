package com.credarc.credarc.service;

import com.credarc.credarc.dto.SignupRequest;
import com.credarc.credarc.dto.SignupResponse;
import com.credarc.credarc.entity.User;
import com.credarc.credarc.exception.DuplicateUserException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordService passwordService;

    public AuthService(UserService userService, PasswordService passwordService) {
        this.userService = userService;
        this.passwordService = passwordService;
    }

    public SignupResponse register(SignupRequest request){

        if (userService.isEmailRegistered(request.getEmail())){
            throw new DuplicateUserException("Email is already registered.");
        }
        if(userService.isMobileRegistered(request.getMobile())){
            throw new DuplicateUserException("Mobile number is already registered.");
        }

        String hashedPassword = passwordService.hashPassword(request.getPassword());
        User user = userService.createUser(request,hashedPassword);
        SignupResponse response = new SignupResponse();

        response.setUserId(user.getUserId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setMessage("User registered successfully.");
        response.setCreatedAt(user.getCreatedAt());

        return response;

    }


}
