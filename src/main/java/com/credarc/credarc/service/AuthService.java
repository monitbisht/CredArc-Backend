package com.credarc.credarc.service;

import com.credarc.credarc.dto.*;
import com.credarc.credarc.entity.Account;
import com.credarc.credarc.entity.User;
import com.credarc.credarc.exception.BadCredentialsException;
import com.credarc.credarc.exception.DuplicateUserException;
import com.credarc.credarc.security.JWTService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordService passwordService;
    private final AccountService accountService;
    private final JWTService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserService userService, PasswordService passwordService, AccountService accountService, JWTService jwtService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.accountService = accountService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
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
        response.setCreatedAt(Instant.now());

        return response;

    }

    @Transactional
    public LoginResponse login(LoginRequest request){

        User user = userService.getUserByEmail(request.getEmail());

        if(! passwordService.matchPassword(request.getPassword(),user.getPassword())){
            throw new BadCredentialsException("Wrong email or password.");
        }

        Account defaultAccount = accountService.defaultAccount(user);
        LoginResponse response = new LoginResponse();

        response.setUserId(user.getUserId());
        List<AccountSummary> accounts = accountService.getAllAccountSummaries(user);
        response.setAccounts(accounts);
        response.setEmail(user.getEmail());
        response.setUserName(user.getName());
        response.setAccessToken(jwtService.generateAccessToken(user));
        response.setRefreshToken(refreshTokenService.issueRefreshToken(user));
        response.setMessage("Login successful.");

        return response;
    }

}
