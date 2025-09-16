package com.vnair.air.dto.request.update;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TicketTypeUpdateRequest {
    @Pattern(regexp = "^(Economy|Business|First)$", message = "Name must be Economy, Business, or First")
    private String name;

    @DecimalMin(value = "1.0", message = "Price multiplier must be at least 1.0")
    @DecimalMax(value = "3.0", message = "Price multiplier must not exceed 3.0")
    private BigDecimal priceMultiplier;
}