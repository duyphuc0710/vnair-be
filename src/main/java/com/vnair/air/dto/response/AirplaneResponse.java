package com.vnair.air.dto.response;

import java.util.Date;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AirplaneResponse {
    private String model;
    private Integer seatCapacity;
    private String airline;
    private Date createdAt;
    private Date updatedAt;
}