package com.vnair.air.dto.response;

import java.math.BigDecimal;
import java.util.Date;

import com.vnair.air.enums.BookingStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BookingResponse {
    private String userEmail;
    private Date bookingDate;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private Date createdAt;
    private Date updatedAt;
}