package com.vnair.air.exception;

import org.springframework.http.HttpStatus;

public class DuplicateAirportCodeException extends BaseAirException {
    public DuplicateAirportCodeException(String message) {
        super("DUPLICATE_AIRPORT_CODE", message, HttpStatus.CONFLICT);
    }
    
    public DuplicateAirportCodeException(String message, Throwable cause) {
        super("DUPLICATE_AIRPORT_CODE", message, HttpStatus.CONFLICT, cause);
    }
}