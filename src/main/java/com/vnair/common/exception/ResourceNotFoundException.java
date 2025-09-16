package com.vnair.common.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

/**
 * Exception thrown when a requested resource is not found
 * Uses BAD_REQUEST (400) status for input validation failures
 */
public class ResourceNotFoundException extends BaseException {
    
    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message, HttpStatus.BAD_REQUEST);
    }
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super("RESOURCE_NOT_FOUND", 
              String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue), 
              HttpStatus.BAD_REQUEST,
              Map.of("resourceName", resourceName, "field", fieldName, "value", fieldValue));
    }

    public ResourceNotFoundException(String message, Map<String, Object> details) {
        super("RESOURCE_NOT_FOUND", message, HttpStatus.BAD_REQUEST, details);
    }
}
