package com.vnair.air.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

/**
 * Exception thrown when a payment is not found
 */
public class PaymentNotFoundException extends BaseAirException {
    
    public PaymentNotFoundException(String message) {
        super("PAYMENT_NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
    
    public PaymentNotFoundException(String message, Map<String, Object> details) {
        super("PAYMENT_NOT_FOUND", message, HttpStatus.NOT_FOUND, details);
    }
    
    public PaymentNotFoundException(String message, Throwable cause) {
        super("PAYMENT_NOT_FOUND", message, HttpStatus.NOT_FOUND, cause);
    }
}