package com.vnair.air.dto.response;

import java.util.Date;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserResponse {
    private String fullName;
    private String email;
    private String phone;
    private String cccdPassport;
    private Date dateOfBirth;
    private Date createdAt;
    private Date updatedAt;
}