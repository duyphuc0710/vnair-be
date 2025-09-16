package com.vnair.air.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AirportCreateRequest {
    @NotBlank(message = "Code must not be blank")
    @Size(min = 3, max = 3, message = "Code must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Code must be 3 uppercase letters")
    private String code;

    @NotBlank(message = "Name must not be blank")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "City must not be blank")
    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;

    @NotBlank(message = "Country must not be blank")
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;
}