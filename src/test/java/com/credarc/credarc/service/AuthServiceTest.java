package com.credarc.credarc.service;

import com.credarc.credarc.dto.*;
import com.credarc.credarc.entity.Account;
import com.credarc.credarc.entity.User;
import com.credarc.credarc.exception.BadCredentialsException;
import com.credarc.credarc.exception.DuplicateUserException;
import com.credarc.credarc.security.JWTService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserService userService;
    @Mock
    private PasswordService passwordService;
    @Mock
    private AccountService accountService;
    @Mock
    private JWTService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;


    @Test
    void register_throwsException_whenEmailAlreadyRegister(){
        signupRequest = new SignupRequest();
        signupRequest.setEmail("email");
        signupRequest.setPassword("password");
        signupRequest.setName("name");
        signupRequest.setMobile("00000000000");

        when(userService.isEmailRegistered(signupRequest.getEmail())).thenReturn(true);

        assertThrows(DuplicateUserException.class,
                ()-> authService.register(signupRequest));

        verify(userService, never()).isMobileRegistered(any());
        verify(userService, never()).createUser(any(), any());
    }

    @Test
    void register_throwsException_whenMobileAlreadyRegistered(){
        signupRequest = new SignupRequest();
        signupRequest.setEmail("email");
        signupRequest.setPassword("password");
        signupRequest.setName("name");
        signupRequest.setMobile("00000000000");

        when(userService.isEmailRegistered(signupRequest.getEmail())).thenReturn(false);
        when(userService.isMobileRegistered(signupRequest.getMobile())).thenReturn(true);

        assertThrows(DuplicateUserException.class,
                ()-> authService.register(signupRequest));

        verify(userService, never()).createUser(any(), any());
    }

    @Test
    void register_success(){
        signupRequest = new SignupRequest();
        signupRequest.setEmail("email@gmail.com");
        signupRequest.setPassword("password");
        signupRequest.setName("name");
        signupRequest.setMobile("00000000000");

        when(userService.isEmailRegistered(signupRequest.getEmail())).thenReturn(false);
        when(userService.isMobileRegistered(signupRequest.getMobile())).thenReturn(false);
        when(passwordService.hashPassword("password")).thenReturn("hashedPw123");

        User mockUser = new User();
        mockUser.setName("name");
        mockUser.setEmail("email@gmail.com");
        when(userService.createUser(signupRequest, "hashedPw123")).thenReturn(mockUser);

        SignupResponse response = authService.register(signupRequest);

        assertNotNull(response);
        assertEquals(mockUser.getName(), response.getName());
        assertEquals(mockUser.getEmail(), response.getEmail());
        assertEquals(mockUser.getUserId(), response.getUserId());
        assertEquals("User registered successfully.", response.getMessage());
        assertNotNull(response.getCreatedAt());

        verify(passwordService).hashPassword("password");
        verify(userService).createUser(signupRequest, "hashedPw123");
    }

    @Test
    void login_throwsException_whenUserNotFound(){
        loginRequest = new LoginRequest();
        ReflectionTestUtils.setField(loginRequest, "email", "email@gmail.com");
        ReflectionTestUtils.setField(loginRequest, "password", "password");

        when(userService.getUserByEmail("email@gmail.com")).thenThrow(new UsernameNotFoundException("User not found with email: notfound@x.com"));

        assertThrows(UsernameNotFoundException.class,
                () -> authService.login(loginRequest));

        verify(passwordService, never()).matchPassword(any(), any());
    }

    @Test
    void login_throwsException_whenPasswordIncorrect(){
        loginRequest = new LoginRequest();
        ReflectionTestUtils.setField(loginRequest, "email", "email@gmail.com");
        ReflectionTestUtils.setField(loginRequest, "password", "wrongPassword");

        User mockUser = new User();
        mockUser.setName("name");
        mockUser.setEmail("email@gmail.com");
        mockUser.setPassword("correctHashedPassword");

        when(userService.getUserByEmail("email@gmail.com")).thenReturn(mockUser);
        when(passwordService.matchPassword("wrongPassword", "correctHashedPassword")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authService.login(loginRequest));

        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void login_success(){
        loginRequest = new LoginRequest();
        ReflectionTestUtils.setField(loginRequest, "email", "email@gmail.com");
        ReflectionTestUtils.setField(loginRequest, "password", "correctHashedPassword");

        User mockUser = new User();
        mockUser.setName("name");
        mockUser.setEmail("email@gmail.com");
        mockUser.setPassword("correctHashedPassword");

        when(userService.getUserByEmail("email@gmail.com")).thenReturn(mockUser);
        when(passwordService.matchPassword("correctHashedPassword", "correctHashedPassword")).thenReturn(true);
        when(jwtService.generateAccessToken(any())).thenReturn("accessToken123");
        when(refreshTokenService.issueRefreshToken(any())).thenReturn("refreshToken123");
        List<AccountSummary> mockAccounts = List.of(new AccountSummary());
        when(accountService.getAllAccountSummaries(mockUser)).thenReturn(mockAccounts);
        when(accountService.defaultAccount(mockUser)).thenReturn(new Account());

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals(mockUser.getName(), response.getUserName());
        assertEquals(mockUser.getEmail(), response.getEmail());
        assertEquals("accessToken123", response.getAccessToken());
        assertEquals("refreshToken123", response.getRefreshToken());
        assertEquals(mockAccounts, response.getAccounts());
        assertEquals("Login successful.", response.getMessage());
    }

    @Test
    void refresh_success(){
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("oldRefreshToken");

        TokenPair pair = new TokenPair();
        pair.setAccessToken("newAccessToken");
        pair.setRefreshToken("newRefreshToken");

        when(refreshTokenService.rotateRefreshToken("oldRefreshToken")).thenReturn(pair);

        TokenRefreshResponse response = authService.refresh(request);

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals("newRefreshToken", response.getRefreshToken());
    }

    @Test
    void logout_success(){
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("someRefreshToken");

        LogoutResponse response = authService.logout(request);

        assertNotNull(response);
        assertEquals("Logout Successful", response.getMessage());
        verify(refreshTokenService).revokeToken("someRefreshToken");
    }
}
