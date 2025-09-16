package com.vnair.air.exception;

import org.springframework.http.HttpStatus;

public class IdempotencyException extends BaseAirException {
    public IdempotencyException(String message, String currentStatus) {
        super("IDEMPOTENCY_VIOLATION", message, HttpStatus.CONFLICT,
              java.util.Map.of("currentPaymentStatus", currentStatus));
    }
}