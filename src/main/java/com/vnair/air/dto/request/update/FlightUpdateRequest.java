package com.vnair.air.dto.request.update;

import java.math.BigDecimal;
import java.util.Date;

import com.vnair.air.enums.FlightStatus;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FlightUpdateRequest {
    private Integer airplaneId;

    private Integer departureAirportId;

    private Integer arrivalAirportId;

    @Future(message = "Departure time must be in the future")
    private Date departureTime;

    private Date arrivalTime;

    @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Base price must have at most 2 decimal places")
    private BigDecimal basePrice;

    private FlightStatus status;
}