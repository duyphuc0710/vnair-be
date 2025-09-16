package com.vnair.air.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class TicketAlreadyBookedException extends BaseAirException {
    public TicketAlreadyBookedException(Long ticketId) {
        super("TICKET_ALREADY_BOOKED", 
              String.format("Ticket %d is already booked", ticketId), 
              HttpStatus.CONFLICT,
              Map.of("ticketId", ticketId));
    }
}