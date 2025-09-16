package com.vnair.air.dto.request.create;

import java.util.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserCreateRequest {
    @NotBlank
    private String fullName;

    @Email
    private String email;

    private String phone;

    @NotBlank
    private String passwordHash;

    private String cccdPassport;

    @PastOrPresent
    private Date dateOfBirth;
}