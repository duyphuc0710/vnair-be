package com.vnair.air.dto.request.update;

import java.math.BigDecimal;
import java.util.Date;

import com.vnair.air.enums.BookingStatus;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BookingUpdateRequest {
    private Long userId;

    @PastOrPresent(message = "Booking date cannot be in the future")
    private Date bookingDate;

    @PositiveOrZero(message = "Total amount must be greater than or equal to 0")
    private BigDecimal totalAmount;

    private BookingStatus status;
}