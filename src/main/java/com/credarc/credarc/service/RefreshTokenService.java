package com.credarc.credarc.service;

import com.credarc.credarc.entity.RefreshToken;
import com.credarc.credarc.entity.User;
import com.credarc.credarc.repository.RefreshTokenRepository;
import com.credarc.credarc.security.JWTService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;

@Service
public class RefreshTokenService {

    private final JWTService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(JWTService jwtService, PasswordService passwordService, RefreshTokenRepository refreshTokenRepository) {
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





}
