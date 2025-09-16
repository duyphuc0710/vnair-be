package com.vnair.air.dto.request.create;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentCreateRequest {
    @NotNull
    private Long bookingId;

    @NotNull
    private BigDecimal amount;

    private String method; // map to PaymentMethod in service

    private String status; // map to PaymentStatus in service

    private Date paidAt;
}