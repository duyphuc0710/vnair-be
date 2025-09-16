package com.vnair.user.service;


import com.vnair.user.dto.Request.auth.SignInRequest;
import com.vnair.user.dto.Response.auth.TokenResponse;


public interface AuthenticationService {
    TokenResponse getAccessToken(SignInRequest request);
    
    TokenResponse getRefreshToken(String request);
}
