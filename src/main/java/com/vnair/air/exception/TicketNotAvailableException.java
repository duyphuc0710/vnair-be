package com.vnair.air.exception;

/**
 * Exception thrown when a ticket is not available for booking
 */
public class TicketNotAvailableException extends RuntimeException {
    
    public TicketNotAvailableException(String message) {
        super(message);
    }
    
    public TicketNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}