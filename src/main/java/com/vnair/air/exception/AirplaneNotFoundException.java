package com.vnair.air.exception;

import org.springframework.http.HttpStatus;

public class AirplaneNotFoundException extends BaseAirException {
    public AirplaneNotFoundException(String message) {
        super("AIRPLANE_NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
    
    public AirplaneNotFoundException(String message, Throwable cause) {
        super("AIRPLANE_NOT_FOUND", message, HttpStatus.NOT_FOUND, cause);
    }
}