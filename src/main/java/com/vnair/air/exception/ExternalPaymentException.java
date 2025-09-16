package com.vnair.air.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class ExternalPaymentException extends BaseAirException {
    public ExternalPaymentException(String message, Throwable cause) {
        super("PAYMENT_GATEWAY_ERROR", message, HttpStatus.BAD_GATEWAY, cause);
    }

    public ExternalPaymentException(String message, String paymentGateway) {
        super("PAYMENT_GATEWAY_ERROR", message, HttpStatus.BAD_GATEWAY,
              Map.of("paymentGateway", paymentGateway));
    }
}