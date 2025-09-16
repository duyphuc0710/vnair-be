package com.vnair.air.dto.request.update;

import com.vnair.air.enums.TicketStatus;

import jakarta.validation.constraints.Pattern;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TicketUpdateRequest {
    private Long flightId;

    private Integer ticketTypeId;

    @Pattern(regexp = "^[0-9]{1,3}[A-Z]$", message = "Seat number format should be like 12A")
    private String seatNumber;

    private TicketStatus status;
}