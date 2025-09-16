package com.vnair.air.dto.request.update;

import java.math.BigDecimal;
import java.util.Date;

import com.vnair.air.enums.PaymentMethod;
import com.vnair.air.enums.PaymentStatus;

import jakarta.validation.constraints.Positive;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentUpdateRequest {
    private Long bookingId;

    @Positive(message = "Amount must be greater than 0")
    private BigDecimal amount;

    private PaymentMethod method;

    private PaymentStatus status;

    private Date paidAt;
}