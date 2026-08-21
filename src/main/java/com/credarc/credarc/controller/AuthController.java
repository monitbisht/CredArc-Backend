package com.credarc.credarc.controller;

import com.credarc.credarc.dto.*;
import com.credarc.credarc.service.AuthService;
import com.credarc.credarc.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
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
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request){
        String clientIp = IpUtils.extractClientIp(request);
        return authService.login(loginRequest,clientIp);
    }

    @PostMapping("/refresh")
    public TokenRefreshResponse refresh(@Valid @RequestBody TokenRefreshRequest tokenRefreshRequest){
        return authService.refresh(tokenRefreshRequest);
    }

    @PostMapping("/logout")
    public LogoutResponse logout(@Valid @RequestBody LogoutRequest logoutRequest){
        return authService.logout(logoutRequest);
    }
}
