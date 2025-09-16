package com.vnair.air.exception;

import org.springframework.http.HttpStatus;

public class AirportNotFoundException extends BaseAirException {
    public AirportNotFoundException(String message) {
        super("AIRPORT_NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
    
    public AirportNotFoundException(String message, Throwable cause) {
        super("AIRPORT_NOT_FOUND", message, HttpStatus.NOT_FOUND, cause);
    }
}