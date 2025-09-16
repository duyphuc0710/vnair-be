package com.vnair.air.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class TicketNotFoundException extends BaseAirException {
    public TicketNotFoundException(String ticketId) {
        super("TICKET_NOT_FOUND", 
              String.format("Ticket not found with ID: %s", ticketId), 
              HttpStatus.NOT_FOUND,
              Map.of("ticketId", ticketId));
    }
}