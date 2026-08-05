package com.credarc.credarc.service;

import com.credarc.credarc.dto.TokenPair;
import com.credarc.credarc.entity.RefreshToken;
import com.credarc.credarc.entity.User;
import com.credarc.credarc.exception.TokenExpiredException;
import com.credarc.credarc.exception.TokenNotFoundException;
import com.credarc.credarc.exception.TokenReuseDetectedException;
import com.credarc.credarc.exception.WrongTokenTypeException;
import com.credarc.credarc.repository.RefreshTokenRepository;
import com.credarc.credarc.security.JWTService;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Mock
    private JWTService jwtService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private User mockUser;

    private User newMockUser() {
        User user = new User();
        user.setEmail("email@gmail.com");
        return user;
    }

    @Test
    void issueRefreshToken_savesEntityAndReturnsRawToken(){
        mockUser = newMockUser();
        when(jwtService.generateRefreshToken(mockUser)).thenReturn("rawRefreshToken");

        String result = refreshTokenService.issueRefreshToken(mockUser);

        assertEquals("rawRefreshToken", result);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken saved = captor.getValue();
        assertEquals(mockUser, saved.getUser());
        assertFalse(saved.isRevoked());
        assertNotEquals("rawRefreshToken", saved.getTokenHash());
        assertNotNull(saved.getIssuedAt());
        assertNotNull(saved.getExpiresAt());
        assertTrue(saved.getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void rotateRefreshToken_throwsTokenExpired_whenJwtExpired(){
        when(jwtService.extractTokenType("expiredToken"))
                .thenThrow(mock(ExpiredJwtException.class));

        assertThrows(TokenExpiredException.class,
                () -> refreshTokenService.rotateRefreshToken("expiredToken"));

        verify(refreshTokenRepository, never()).findByTokenHash(any());
    }

    @Test
    void rotateRefreshToken_throwsWrongTokenType_whenNotRefreshToken(){
        when(jwtService.extractTokenType("accessToken")).thenReturn("access");

        assertThrows(WrongTokenTypeException.class,
                () -> refreshTokenService.rotateRefreshToken("accessToken"));

        verify(refreshTokenRepository, never()).findByTokenHash(any());
    }

    @Test
    void rotateRefreshToken_throwsTokenNotFound_whenHashNotInRepository(){
        when(jwtService.extractTokenType("unknownToken")).thenReturn("refresh");
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThrows(TokenNotFoundException.class,
                () -> refreshTokenService.rotateRefreshToken("unknownToken"));
    }

    @Test
    void rotateRefreshToken_throwsTokenReuseDetected_andRevokesAllSessions_whenAlreadyRevoked(){
        mockUser = newMockUser();
        RefreshToken storedToken = new RefreshToken();
        storedToken.setUser(mockUser);
        storedToken.setRevoked(true);
        storedToken.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));

        when(jwtService.extractTokenType("reusedToken")).thenReturn("refresh");
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

        assertThrows(TokenReuseDetectedException.class,
                () -> refreshTokenService.rotateRefreshToken("reusedToken"));

        verify(refreshTokenRepository).revokeAllActiveForUser(mockUser);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rotateRefreshToken_throwsTokenExpired_whenDbExpiryPassed(){
        mockUser = newMockUser();
        RefreshToken storedToken = new RefreshToken();
        storedToken.setUser(mockUser);
        storedToken.setRevoked(false);
        storedToken.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));

        when(jwtService.extractTokenType("expiredDbToken")).thenReturn("refresh");
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

        assertThrows(TokenExpiredException.class,
                () -> refreshTokenService.rotateRefreshToken("expiredDbToken"));

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rotateRefreshToken_success_revokesOldAndIssuesNewPair(){
        mockUser = newMockUser();
        RefreshToken storedToken = new RefreshToken();
        storedToken.setUser(mockUser);
        storedToken.setRevoked(false);
        storedToken.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));

        when(jwtService.extractTokenType("validOldToken")).thenReturn("refresh");
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));
        when(jwtService.generateRefreshToken(mockUser)).thenReturn("newRawRefreshToken");
        when(jwtService.generateAccessToken(mockUser)).thenReturn("newAccessToken");

        TokenPair result = refreshTokenService.rotateRefreshToken("validOldToken");

        assertNotNull(result);
        assertEquals("newAccessToken", result.getAccessToken());
        assertEquals("newRawRefreshToken", result.getRefreshToken());
        assertTrue(storedToken.isRevoked());

        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void revokeToken_revokesAndSaves_whenTokenFound(){
        RefreshToken storedToken = new RefreshToken();
        storedToken.setRevoked(false);

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

        refreshTokenService.revokeToken("someToken");

        assertTrue(storedToken.isRevoked());
        verify(refreshTokenRepository).save(storedToken);
    }

    @Test
    void revokeToken_doesNothing_whenTokenNotFound(){
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> refreshTokenService.revokeToken("unknownToken"));

        verify(refreshTokenRepository, never()).save(any());
    }
}