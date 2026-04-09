package com.credarc.credarc.security;


import com.credarc.credarc.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;

@Service
public class JWTService {

    @Value("${jwt.secret}")
    private  String SECRET_KEY ;


    public String generateToken(User user){
        HashMap<String,Object> extraClaims =  new HashMap<>();
        extraClaims.put("email" , user.getEmail());

        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getUserId().toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, CustomUserDetails userDetails){
        final String tokenUserId = extractUserId(token);
        return tokenUserId.equals(userDetails.getUserId().toString())
                && !isTokenExpired(token);
    }
    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    private String extractUserId(String token){
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    private Date extractExpiration(String token){
        return extractAllClaims(token).getExpiration();
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }
}
