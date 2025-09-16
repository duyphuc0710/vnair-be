package com.vnair.air.dto.request.create;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.vnair.air.enums.BookingStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BookingCreateRequest {
    @NotNull(message = "User ID must not be null")
    private Long userId;

    @PastOrPresent(message = "Booking date cannot be in the future")
    private Date bookingDate;

    @PositiveOrZero(message = "Total amount must be greater than or equal to 0")
    private BigDecimal totalAmount;

    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING; // Default status

    // ticket ids that belong to this booking
    private List<Long> ticketIds;
}