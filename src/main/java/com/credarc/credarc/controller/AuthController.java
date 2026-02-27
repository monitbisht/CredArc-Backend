package com.credarc.credarc.controller;

import com.credarc.credarc.dto.SignupRequest;
import com.credarc.credarc.dto.SignupResponse;
import com.credarc.credarc.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(@Valid @RequestBody SignupRequest signupRequest){
        return authService.register(signupRequest);
    }

    @PostMapping("/login")
    public void login(){
        //TODO
    }
}
