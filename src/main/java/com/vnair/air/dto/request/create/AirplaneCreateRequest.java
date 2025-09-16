package com.vnair.air.dto.request.create;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AirplaneCreateRequest {
    @NotBlank(message = "Model must not be blank")
    @Size(max = 50, message = "Model must not exceed 50 characters")
    private String model;

    @Min(value = 50, message = "Seat capacity must be at least 50")
    @Max(value = 600, message = "Seat capacity must not exceed 600")
    private Integer seatCapacity;

    @NotBlank(message = "Airline must not be blank")
    @Size(max = 50, message = "Airline must not exceed 50 characters")
    private String airline;
}