package com.vnair.air.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class InvalidOperationException extends BaseAirException {
    public InvalidOperationException(String message) {
        super("INVALID_OPERATION", message, HttpStatus.BAD_REQUEST);
    }

    public InvalidOperationException(String message, Map<String, Object> details) {
        super("INVALID_OPERATION", message, HttpStatus.BAD_REQUEST, details);
    }
}