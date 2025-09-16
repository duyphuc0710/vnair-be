package com.vnair.common.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.security.sasl.AuthenticationException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.vnair.air.exception.ExternalPaymentException;
import com.vnair.air.exception.FlightNotFoundException;
import com.vnair.air.exception.TicketAlreadyBookedException;

import org.springframework.web.bind.MissingServletRequestParameterException;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Global Exception Handler for consistent error handling across the application
 * 
 * Exception Hierarchy:
 * 1. BaseException - Main handler for all custom exceptions (highest priority)
 * 2. Framework Validation Exceptions (Bean Validation, Request Validation)
 * 3. Framework HTTP Exceptions (Method not allowed, etc.)
 * 4. Security Exceptions (Authentication, Authorization)
 * 5. Specific Exception Documentation (for Swagger)
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {

    // ==================== MAIN BASE EXCEPTION HANDLER ====================
    
    /**
     * Main handler for all custom BaseException and its subclasses
     * This provides consistent handling for all custom exceptions
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        String traceId = getTraceId();
        log.error("Custom exception - Code: {}, Message: {}, TraceId: {}", 
                 ex.getErrorCode(), ex.getMessage(), traceId, ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .data(ex.getDetails())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(traceId)
                .build();
                
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    // ==================== FRAMEWORK VALIDATION EXCEPTIONS ====================
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        log.warn("Validation failed for request: {} - Errors: {}", request.getRequestURI(), fieldErrors);

        return ErrorResponse.builder()
                .message("Invalid request data")
                .errorCode("VALIDATION_ERROR")
                .fieldErrors(fieldErrors)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(getTraceId())
                .build();
    }

    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class,
        IllegalArgumentException.class,
        PropertyReferenceException.class,
        HttpMessageNotReadableException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestExceptions(Exception ex, HttpServletRequest request) {
        String errorCode = "BAD_REQUEST";
        String message = ex.getMessage();

        if (ex instanceof MethodArgumentTypeMismatchException mismatchEx) {
            errorCode = "ARGUMENT_TYPE_MISMATCH";
            String requiredType = mismatchEx.getRequiredType() != null ? 
                                  mismatchEx.getRequiredType().getSimpleName() : "unknown";
            message = String.format("Parameter '%s' value '%s' type mismatch. Required: %s",
                    mismatchEx.getName(), mismatchEx.getValue(), requiredType);
            log.warn("MethodArgumentTypeMismatchException: {}", message);
        } else if (ex instanceof IllegalArgumentException) {
            errorCode = "ILLEGAL_ARGUMENT";
            message = "Invalid argument: " + ex.getMessage();
            log.warn("IllegalArgumentException: {}", ex.getMessage());
        } else if (ex instanceof PropertyReferenceException) {
            errorCode = "INVALID_SORT_PROPERTY";
            message = "Invalid sort property: " + ex.getMessage();
            log.warn("PropertyReferenceException: {}", ex.getMessage());
        } else if (ex instanceof HttpMessageNotReadableException notReadableEx) {
            errorCode = "MALFORMED_JSON";
            String detail = notReadableEx.getMostSpecificCause() != null
                    ? notReadableEx.getMostSpecificCause().getMessage()
                    : notReadableEx.getMessage();
            message = "Malformed or unreadable request body";
            if (detail != null && !detail.equals(message)) {
                message += ": " + detail;
            }
            log.warn("Malformed JSON request: {}", detail);
        }

        return ErrorResponse.builder()
                .message(message)
                .errorCode(errorCode)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(getTraceId())
                .build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = String.format("Missing required parameter: %s", ex.getParameterName());
        log.warn("MissingServletRequestParameterException: {}", message);

        return ErrorResponse.builder()
                .message(message)
                .errorCode("MISSING_PARAMETER")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(getTraceId())
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        String message = "Constraint violation: " + ex.getMessage();
        log.warn("ConstraintViolationException: {}", message);

        return ErrorResponse.builder()
                .message(message)
                .errorCode("CONSTRAINT_VIOLATION")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(getTraceId())
                .build();
    }

    @ExceptionHandler(InvalidFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidFormatException(InvalidFormatException ex, HttpServletRequest request) {
        log.warn("InvalidFormatException: {}", ex.getMessage());

        String fieldName = ex.getPath().isEmpty() ? "unknown" : ex.getPath().get(0).getFieldName();
        String targetType = ex.getTargetType().getSimpleName();
        String value = ex.getValue() != null ? ex.getValue().toString() : "null";
        String message = String.format("Invalid value '%s' for field '%s'. Expected type: %s. Accepted values: %s",
                value, fieldName, targetType,
                ex.getTargetType().isEnum() ? java.util.Arrays.toString(ex.getTargetType().getEnumConstants())
                        : targetType);

        return ErrorResponse.builder()
                .message(message)
                .errorCode("INVALID_FORMAT")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(getTraceId())
                .build();
    }

    // ==================== FRAMEWORK HTTP EXCEPTIONS ====================
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String message = String.format("Method %s not supported. Supported: %s", ex.getMethod(), ex.getSupportedHttpMethods());

        return ErrorResponse.builder()
                .message(message)
                .errorCode("METHOD_NOT_ALLOWED")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(getTraceId())
                .build();
    }

    // ==================== SECURITY EXCEPTIONS ====================
    
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ErrorResponse.builder()
                .message("Authentication failed or credentials invalid")
                .errorCode("AUTHENTICATION_FAILED")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(getTraceId())
                .build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return ErrorResponse.builder()
                .message("Access denied")
                .errorCode("ACCESS_DENIED")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(getTraceId())
                .build();
    }

    // ==================== SPECIFIC EXCEPTION DOCUMENTATION ====================
    
    @ExceptionHandler(FlightNotFoundException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Flight Not Found",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Flight not found",
                                    summary = "Handle Flight Not Found",
                                    value = """
                                            {
                                                 "timestamp": "2025-09-13T21:00:00Z",
                                                 "status": 404,
                                                 "error": "Not Found",
                                                 "code": "FLIGHT_NOT_FOUND",
                                                 "message": "Flight not found with ID: 123",
                                                 "details": {"flightId": "123"},
                                                 "path": "/api/flights/123",
                                                 "traceId": "xxxx-yyy"
                                             }
                                            """
                            ))})
    })
    public ResponseEntity<ErrorResponse> handleFlightNotFoundException(FlightNotFoundException ex, HttpServletRequest request) {
        return handleBaseException(ex, request);
    }

    @ExceptionHandler(TicketAlreadyBookedException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "409", description = "Ticket Already Booked",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Ticket already booked",
                                    summary = "Handle Ticket Already Booked",
                                    value = """
                                            {
                                                 "timestamp": "2025-09-13T21:00:00Z",
                                                 "status": 409,
                                                 "error": "Conflict",
                                                 "code": "TICKET_ALREADY_BOOKED",
                                                 "message": "Ticket 123 is already booked",
                                                 "details": {"ticketId": 123},
                                                 "path": "/api/bookings",
                                                 "traceId": "xxxx-yyy"
                                             }
                                            """
                            ))})
    })
    public ResponseEntity<ErrorResponse> handleTicketAlreadyBookedException(TicketAlreadyBookedException ex, HttpServletRequest request) {
        return handleBaseException(ex, request);
    }

    @ExceptionHandler(ExternalPaymentException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "502", description = "Payment Gateway Error",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Payment gateway error",
                                    summary = "Handle Payment Gateway Error",
                                    value = """
                                            {
                                                 "timestamp": "2025-09-13T21:00:00Z",
                                                 "status": 502,
                                                 "error": "Bad Gateway",
                                                 "code": "PAYMENT_GATEWAY_ERROR",
                                                 "message": "Payment gateway temporarily unavailable",
                                                 "path": "/api/payments",
                                                 "traceId": "xxxx-yyy"
                                             }
                                            """
                            ))})
    })
    public ResponseEntity<ErrorResponse> handleExternalPaymentException(ExternalPaymentException ex, HttpServletRequest request) {
        return handleBaseException(ex, request);
    }

    // ==================== UTILITY METHOD ====================
    
    private String getTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

}