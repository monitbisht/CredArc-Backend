package com.credarc.credarc.service;

import com.credarc.credarc.dto.TokenPair;
import com.credarc.credarc.entity.RefreshToken;
import com.credarc.credarc.entity.User;
import com.credarc.credarc.exception.*;
import com.credarc.credarc.repository.RefreshTokenRepository;
import com.credarc.credarc.security.JWTService;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;

@Service
public class RefreshTokenService {

    private final JWTService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(JWTService jwtService, RefreshTokenRepository refreshTokenRepository) {
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String issueRefreshToken(User user)  {
       String refreshToken = jwtService.generateRefreshToken(user);
       String hashedToken = hashToken(refreshToken);
       RefreshToken refreshTokenEntity = new RefreshToken();
       refreshTokenEntity.setUser(user);
       refreshTokenEntity.setTokenHash(hashedToken);
       refreshTokenEntity.setRevoked(false);
       refreshTokenEntity.setIssuedAt(Instant.now());
       refreshTokenEntity.setExpiresAt(Instant.now().plus(Duration.ofDays(7)));

       refreshTokenRepository.save(refreshTokenEntity);

       return refreshToken;
    }

    @Transactional(noRollbackFor = TokenReuseDetectedException.class)
    public TokenPair rotateRefreshToken(String currentRefreshToken) {
        String tokenType;
        try {
            tokenType = jwtService.extractTokenType(currentRefreshToken);
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Refresh token expired.");
        }

        if (!"refresh".equalsIgnoreCase(tokenType)) {
            throw new WrongTokenTypeException("Provided token is not a refresh token.");
        }

        String currentHashedToken = hashToken(currentRefreshToken);
        RefreshToken storedRefreshToken = refreshTokenRepository.findByTokenHash(currentHashedToken).
                                            orElseThrow(()-> new TokenNotFoundException("Refresh token not found."));

        if(storedRefreshToken.isRevoked()) {
            refreshTokenRepository.revokeAllActiveForUser(storedRefreshToken.getUser());
            throw new TokenReuseDetectedException("Refresh token reuse detected. All sessions revoked.");
        }

        if(checkTokenExpiry(storedRefreshToken.getExpiresAt())) {
            throw new TokenExpiredException("Refresh token expired.");
        }

        storedRefreshToken.setRevoked(true);
        refreshTokenRepository.save(storedRefreshToken);

        String newRefreshToken = issueRefreshToken(storedRefreshToken.getUser());
        String newAccessToken = jwtService.generateAccessToken(storedRefreshToken.getUser());

        TokenPair tokenPair = new TokenPair();
        tokenPair.setRefreshToken(newRefreshToken);
        tokenPair.setAccessToken(newAccessToken);
        return tokenPair;

    }


    /** Helper Methods **/
    private String hashToken(String token){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for(byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e){
            throw new IllegalStateException("SHA-256 algorithm not found",e);
        }

    }

    private boolean checkTokenExpiry(Instant expiry){
        return expiry.isBefore(Instant.now());
    }





}
