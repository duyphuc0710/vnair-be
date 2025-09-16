package com.vnair.air.dto.response;

import java.math.BigDecimal;
import java.util.Date;

import com.vnair.air.enums.PaymentMethod;
import com.vnair.air.enums.PaymentStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentResponse {
    private String bookingCode;
    private BigDecimal amount;
    private String transactionId;
    private PaymentMethod method;
    private PaymentStatus status;
    private Date paidAt;
    private Date createdAt;
    private Date updatedAt;
}