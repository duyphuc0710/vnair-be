package com.vnair.air.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class PaymentFailedException extends BaseAirException {
    public PaymentFailedException(String message, String bookingId) {
        super("PAYMENT_FAILED", message, HttpStatus.PAYMENT_REQUIRED,
              Map.of("bookingId", bookingId));
    }

    public PaymentFailedException(String message, Throwable cause) {
        super("PAYMENT_FAILED", message, HttpStatus.PAYMENT_REQUIRED, cause);
    }
}