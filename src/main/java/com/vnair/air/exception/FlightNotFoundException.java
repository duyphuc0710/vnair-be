package com.vnair.air.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class FlightNotFoundException extends BaseAirException {
    public FlightNotFoundException(String flightId) {
        super("FLIGHT_NOT_FOUND", 
              String.format("Flight not found with ID: %s", flightId), 
              HttpStatus.NOT_FOUND,
              Map.of("flightId", flightId));
    }
}