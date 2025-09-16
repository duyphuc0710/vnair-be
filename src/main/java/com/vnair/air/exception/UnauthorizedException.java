package com.vnair.air.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseAirException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }
}