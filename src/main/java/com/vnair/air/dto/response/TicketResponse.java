package com.vnair.air.dto.response;

import java.util.Date;

import com.vnair.air.enums.TicketStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TicketResponse {
    private String flight; // flight code or route description
    private String ticketType; // ticket type name
    private String seatNumber;
    private TicketStatus status;
    private Date createdAt;
    private Date updatedAt;
}