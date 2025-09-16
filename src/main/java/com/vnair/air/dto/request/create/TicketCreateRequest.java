package com.vnair.air.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TicketCreateRequest {
    @NotNull
    private Long flightId;

    @NotNull
    private Integer ticketTypeId;

    @NotBlank
    private String seatNumber;

    private String status; // map to TicketStatus in service
}