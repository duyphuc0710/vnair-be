package com.vnair.air.exception;

import com.vnair.common.exception.BaseException;
import org.springframework.http.HttpStatus;
import java.util.Map;

/**
 * Base exception for all Air module specific exceptions
 * Extends the common BaseException to maintain consistency
 */
public abstract class BaseAirException extends BaseException {

    protected BaseAirException(String errorCode, String message, HttpStatus httpStatus) {
        super(errorCode, message, httpStatus);
    }

    protected BaseAirException(String errorCode, String message, HttpStatus httpStatus, Map<String, Object> details) {
        super(errorCode, message, httpStatus, details);
    }

    protected BaseAirException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(errorCode, message, httpStatus, cause);
    }

    protected BaseAirException(String errorCode, String message, HttpStatus httpStatus, Map<String, Object> details, Throwable cause) {
        super(errorCode, message, httpStatus, details, cause);
    }
}