package com.vnair.air.dto.response;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TicketTypeResponse {
    private String name; // Economy, Business, First
    private BigDecimal priceMultiplier;
    private Date createdAt;
    private Date updatedAt;
}