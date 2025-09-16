package com.vnair.air.dto.response;

import java.math.BigDecimal;
import java.util.Date;

import com.vnair.air.enums.FlightStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FlightResponse {
    private String airplane; // airplane model name or code
    private String departureAirport; // airport code
    private String arrivalAirport; // airport code
    private Date departureTime;
    private Date arrivalTime;
    private BigDecimal basePrice;
    private FlightStatus status;
    private Date createdAt;
    private Date updatedAt;
}