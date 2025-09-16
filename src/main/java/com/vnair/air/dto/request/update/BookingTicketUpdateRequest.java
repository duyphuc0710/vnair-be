package com.vnair.air.dto.request.update;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BookingTicketUpdateRequest {
    private Long bookingId;

    private Long ticketId;
}