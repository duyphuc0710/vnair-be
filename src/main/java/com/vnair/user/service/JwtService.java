package com.vnair.user.service;

import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.vnair.user.common.enums.token.TokenType;

public interface JwtService {
    
    String generateAccessToken(long userId, String username, Collection<? extends GrantedAuthority> authorities);

    String generateRefreshToken(long userId, String username, Collection<? extends GrantedAuthority> authorities);

    String extractUsername(String token, TokenType tokenType);
    
    Long extractUserId(String token, TokenType tokenType);
    
    Date extractExpiration(String token, TokenType tokenType);
    
    boolean isTokenExpired(String token, TokenType tokenType);
    
    boolean validateToken(String token, UserDetails userDetails, TokenType tokenType);
}
