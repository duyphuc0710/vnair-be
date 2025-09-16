package com.vnair.air.exception;

import org.springframework.http.HttpStatus;

public class TicketTypeNotFoundException extends BaseAirException {
    public TicketTypeNotFoundException(String message) {
        super("TICKET_TYPE_NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
    
    public TicketTypeNotFoundException(String message, Throwable cause) {
        super("TICKET_TYPE_NOT_FOUND", message, HttpStatus.NOT_FOUND, cause);
    }
}