package com.vnair.user.config;

import jakarta.servlet.*;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vnair.user.model.UserEntity;

import java.io.IOException;

public class LogbackConfig implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                String userId = "";
                String username = "";
                if (principal instanceof UserEntity userEntity) {
                    userId = String.valueOf(userEntity.getId());
                    username = userEntity.getUsername();
                } else if (principal instanceof String str) {
                    userId = str;
                    username = str;
                }
                MDC.put("userId", userId);
                MDC.put("username", username);
            }
            chain.doFilter(request, response);
        } finally {
            MDC.remove("userId");
            MDC.remove("username");
        }
    }
    
    // @Override
    // public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
    //     throws IOException, ServletException {
    //     try {
    //         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //         if (auth != null && auth.isAuthenticated()) {
    //             String userId = auth.getName(); 
    //             MDC.put("userId", userId);
    //         }
    //         chain.doFilter(request, response);
    //     } finally {
    //         MDC.remove("userId");
    //     }
    // }
}
