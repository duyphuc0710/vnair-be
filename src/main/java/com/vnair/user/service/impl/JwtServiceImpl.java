package com.vnair.user.service.impl;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.vnair.user.common.enums.token.TokenType;
import com.vnair.user.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j(topic = "JWT-SERVICE")
public class JwtServiceImpl implements JwtService{

    @Value("${jwt.expirationMinute}")
    private Long expirationMinute;

    @Value("${jwt.expirationDate}")
    private Long expirationDate;

    @Value("${jwt.accessKey}")
    private String accessKey;

    @Value("${jwt.refreshKey}")
    private String refreshKey;

    @Override
    public String generateAccessToken(long userId, String username,
            Collection<? extends GrantedAuthority> authorities) {
        
        log.info("Generate access token for userId={}, username={}", userId, username);        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        
        // Extract only role names (not detailed permissions) to keep token small
        if (authorities != null) {
            List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))  // Only include roles, not detailed permissions
                .collect(java.util.stream.Collectors.toList());
            claims.put("roles", roles);
        }

        return generateAccessToken(claims, username);
    }
    

    @Override
    public String generateRefreshToken(long userId, String username,
            Collection<? extends GrantedAuthority> authorities) {
        
        log.info("Generate refresh token for userId={}, username={}", userId, username);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        
        // For refresh token, we only need minimal info
        // Detailed permissions will be loaded when refreshing access token
        
        return generateRefreshToken(claims, username);
    }

    @Override
    public String extractUsername(String token, TokenType tokenType) {
        log.info("Extract username from token type: {}", tokenType);
        return extractClaims(token, tokenType, Claims::getSubject);
    }

    @Override
    public Long extractUserId(String token, TokenType tokenType) {
        log.info("Extract userId from token type: {}", tokenType);
        return extractClaims(token, tokenType, claims -> ((Number) claims.get("userId")).longValue());
    }

    @Override
    public Date extractExpiration(String token, TokenType tokenType) {
        return extractClaims(token, tokenType, Claims::getExpiration);
    }

    @Override
    public boolean isTokenExpired(String token, TokenType tokenType) {
        try {
            return extractExpiration(token, tokenType).before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    @Override
    public boolean validateToken(String token, UserDetails userDetails, TokenType tokenType) {
        try {
            final String username = extractUsername(token, tokenType);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token, tokenType));
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private <T> T extractClaims(String token, TokenType tokenType, Function<Claims, T> claimsExtractor){
        final Claims claims = extractAllClaims(token, tokenType);
        return claimsExtractor.apply(claims);
    }

    private Claims extractAllClaims(String token, TokenType tokenType) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(getKey(tokenType))
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            throw new AccessDeniedException("JWT token is expired", e);
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw new AccessDeniedException("Invalid JWT signature", e);
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new AccessDeniedException("Invalid JWT token", e);
        }
    }

    private String generateAccessToken(Map<String, Object> claims, String username){
        log.info("Generate access token for user: {}", username);
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + (expirationMinute * 60 * 1000)))
            .signWith(getKey(TokenType.ACCESS_TOKEN), SignatureAlgorithm.HS256)
            .compact();
    }

    private String generateRefreshToken(Map<String, Object> claims, String username){
        log.info("Generate refresh token for user: {}", username);
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + (expirationDate * 24 * 60 * 60 * 1000)))
            .signWith(getKey(TokenType.REFRESH_TOKEN), SignatureAlgorithm.HS256)
            .compact();
    }
    
    private Key getKey(TokenType tokenType){
        switch (tokenType) {
            case ACCESS_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessKey));
            }
            case REFRESH_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshKey));
            }
            default -> throw new IllegalArgumentException("Invalid token type: " + tokenType);
        }
    }
}
