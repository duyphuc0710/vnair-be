package com.vnair.air.dto.request.update;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AirportUpdateRequest {
    @Size(min = 3, max = 3, message = "Code must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Code must be 3 uppercase letters")
    private String code;

    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;

    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;
}