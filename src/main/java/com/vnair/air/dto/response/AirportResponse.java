package com.vnair.air.dto.response;

import java.util.Date;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AirportResponse {
    private String code;
    private String name;
    private String city;
    private String country;
    private Date createdAt;
    private Date updatedAt;
}