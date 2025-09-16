package com.vnair.user.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.vnair.common.exception.ResourceNotFoundException;
import com.vnair.user.common.enums.token.TokenType;
import com.vnair.user.dto.Request.auth.SignInRequest;
import com.vnair.user.dto.Response.auth.TokenResponse;
import com.vnair.user.model.UserEntity;
import com.vnair.user.repository.UserRepository;
import com.vnair.user.service.AuthenticationService;
import com.vnair.user.service.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j(topic = "AUTHENTICATION-SERVICE")
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public TokenResponse getAccessToken(SignInRequest request) {
        log.info("Authentication request for username: {}", request.getUsername());

        try {
            // Authenticate user with username and password
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            // Get authenticated user details
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            log.info("User authenticated successfully: {}", userEntity.getUsername());

            // Generate access token and refresh token
            String accessToken = jwtService.generateAccessToken(
                userEntity.getId(), 
                userEntity.getUsername(), 
                userEntity.getAuthorities()
            );
            
            String refreshToken = jwtService.generateRefreshToken(
                userEntity.getId(), 
                userEntity.getUsername(), 
                userEntity.getAuthorities()
            );

            log.info("Tokens generated successfully for user: {}", userEntity.getUsername());

            return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
                
        } catch (AuthenticationException e) {
            log.error("Authentication failed for username: {}. Error: {}", request.getUsername(), e.getMessage());
            throw new RuntimeException("Invalid username or password", e);
        } catch (Exception e) {
            log.error("Unexpected error during authentication for username: {}. Error: {}", request.getUsername(), e.getMessage());
            throw new RuntimeException("Authentication service error", e);
        }
    }

    @Override
    public TokenResponse getRefreshToken(String refreshToken) {
        log.info("Refresh token request received");
        
        try {
            // Extract username from refresh token
            String username = jwtService.extractUsername(refreshToken, TokenType.REFRESH_TOKEN);
            log.info("Username extracted from refresh token: {}", username);
            
            // Load user from database
            UserEntity userEntity = userRepository.findByUsernameWithRolesAndPermissions(username)
                .orElseThrow(() -> {
                    log.error("User not found for username: {}", username);
                    return new ResourceNotFoundException("User not found");
                });
                
            // Validate refresh token
            if (jwtService.validateToken(refreshToken, userEntity, TokenType.REFRESH_TOKEN)) {
                log.info("Refresh token is valid for user: {}", username);
                
                // Generate new access token
                String newAccessToken = jwtService.generateAccessToken(
                    userEntity.getId(),
                    userEntity.getUsername(),
                    userEntity.getAuthorities()
                );
                
                // Generate new refresh token
                String newRefreshToken = jwtService.generateRefreshToken(
                    userEntity.getId(),
                    userEntity.getUsername(),
                    userEntity.getAuthorities()
                );
                
                log.info("New tokens generated successfully for user: {}", username);
                
                return TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();
                    
            } else {
                log.error("Invalid refresh token for user: {}", username);
                throw new RuntimeException("Invalid refresh token");
            }
            
        } catch (Exception e) {
            log.error("Error processing refresh token: {}", e.getMessage());
            throw new RuntimeException("Failed to refresh token", e);
        }
    }
}
