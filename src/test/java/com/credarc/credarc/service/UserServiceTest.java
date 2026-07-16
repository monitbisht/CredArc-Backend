package com.credarc.credarc.service;

import com.credarc.credarc.dto.SignupRequest;
import com.credarc.credarc.entity.User;
import com.credarc.credarc.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;


    @Mock
    private User mockUser;
    @Mock
    private SignupRequest signupRequest;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setName("Monit");
        signupRequest.setMobile("9876543210");
        signupRequest.setEmail("monit@gmail.com");

        mockUser = new User();
        mockUser.setName("Monit");
        mockUser.setMobile("9876543210");
        mockUser.setEmail("monit@gmail.com");
        mockUser.setPassword("y43e28d93");
        mockUser.setCreatedAt(Instant.now());
    }

    @Test
    void isEmailRegisteredTest() {
        when(userRepository.existsByEmail("monit@gmail.com")).thenReturn(true);
        assertTrue((userService.isEmailRegistered("monit@gmail.com")));
    }

    @Test
    void isMobileRegisteredTest() {
        when(userRepository.existsByMobile("9876543210")).thenReturn(true);
        assertTrue((userService.isMobileRegistered("9876543210")));
    }

    @Test
    void createUserTest() {
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User res = userService.createUser(signupRequest,"y43e28d93");
        assertNotNull(res);
        assertEquals("Monit",res.getName());
    }

    @Test
    void getUserByEmailTest() {
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(mockUser));

        User res = userService.getUserByEmail(mockUser.getEmail());
        assertNotNull(res);
        assertEquals("Monit",res.getName());
    }

    @Test
    void getUserByEmail_throwsException_whenUserNotFound() {
        when(userRepository.findByEmail("notfound@x.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.getUserByEmail("notfound@x.com"));
    }

    @Test
    void getUserByUserIdTest() {
        when(userRepository.findByUserId(any(UUID.class))).thenReturn(Optional.of(mockUser));

        User res = userService.getUserByUserId(UUID.randomUUID());

        assertNotNull(res);
        assertEquals("Monit",res.getName());
    }
}