package com.vnair.air.dto.request.create;

import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BookingTicketCreateRequest {
    @NotNull
    private Long bookingId;

    @NotNull
    private Long ticketId;
}