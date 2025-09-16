package com.vnair.air.dto.request.update;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AirplaneUpdateRequest {
    @Size(max = 50, message = "Model must not exceed 50 characters")
    private String model;

    @Min(value = 50, message = "Seat capacity must be at least 50")
    @Max(value = 600, message = "Seat capacity must not exceed 600")
    private Integer seatCapacity;

    @Size(max = 50, message = "Airline must not exceed 50 characters")
    private String airline;
}