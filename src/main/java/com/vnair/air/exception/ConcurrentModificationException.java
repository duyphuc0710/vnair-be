package com.vnair.air.exception;

import org.springframework.http.HttpStatus;

public class ConcurrentModificationException extends BaseAirException {
    public ConcurrentModificationException(String message) {
        super("CONCURRENT_MODIFICATION", message, HttpStatus.CONFLICT);
    }
}