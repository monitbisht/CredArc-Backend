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

    private User mockUser;
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
    void createUserTest() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User res = userService.createUser(signupRequest,"y43e28d93");
        assertNotNull(res);
        assertEquals(signupRequest.getName(),res.getName());
        assertEquals(signupRequest.getMobile(),res.getMobile());
        assertEquals(signupRequest.getEmail(),res.getEmail());
        assertEquals("y43e28d93",res.getPassword());
        assertNotNull(res.getCreatedAt());
    }

    @Test
    void getUserByEmailTest() {
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(mockUser));

        User res = userService.getUserByEmail(mockUser.getEmail());
        assertNotNull(res);
        assertEquals(mockUser, res);
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

    @Test
    void getUserByUserId_throwsException_whenNotFound() {
        UUID missingId = UUID.randomUUID();
        when(userRepository.findByUserId(missingId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.getUserByUserId(missingId));
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
    void isEmailRegistered_returnsFalse_whenNotRegistered(){
        when(userRepository.existsByEmail("notfound@gmail.com")).thenReturn(false);
        assertFalse(userService.isEmailRegistered("notfound@gmail.com"));
    }

    @Test
    void isMobileRegistered_returnsFalse_whenNotRegistered() {
        when(userRepository.existsByMobile("0000000000")).thenReturn(false);
        assertFalse(userService.isMobileRegistered("0000000000"));
    }
}