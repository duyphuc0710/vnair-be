package com.vnair.air.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class BookingNotFoundException extends BaseAirException {
    public BookingNotFoundException(String bookingId) {
        super("BOOKING_NOT_FOUND", 
              String.format("Booking not found with ID: %s", bookingId), 
              HttpStatus.NOT_FOUND,
              Map.of("bookingId", bookingId));
    }
}