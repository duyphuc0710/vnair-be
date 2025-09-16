package com.vnair.air.dto.request.create;

import java.math.BigDecimal;
import java.util.Date;

import com.vnair.air.enums.FlightStatus;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FlightCreateRequest {
    @NotNull(message = "Airplane ID must not be null")
    private Integer airplaneId;

    @NotNull(message = "Departure airport ID must not be null")
    private Integer departureAirportId;

    @NotNull(message = "Arrival airport ID must not be null")
    private Integer arrivalAirportId;

    @NotNull(message = "Departure time must not be null")
    @Future(message = "Departure time must be in the future")
    private Date departureTime;

    @NotNull(message = "Arrival time must not be null")
    private Date arrivalTime;

    @NotNull(message = "Base price must not be null")
    @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Base price must have at most 2 decimal places")
    private BigDecimal basePrice;

    @Builder.Default
    private FlightStatus status = FlightStatus.SCHEDULED; // Default SCHEDULED
}