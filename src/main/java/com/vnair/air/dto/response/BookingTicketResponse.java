package com.vnair.air.dto.response;

import java.util.Date;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BookingTicketResponse {
    private String bookingCode; // auto-generated booking code instead of id
    private String ticketSeat; // seat number like 12A
    private Date createdAt;
    private Date updatedAt;
}