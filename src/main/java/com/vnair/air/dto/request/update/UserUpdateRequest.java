package com.vnair.air.dto.request.update;

import java.util.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserUpdateRequest {
    @Size(min = 2, max = 100)
    private String fullName;

    @Email
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone must be 10-15 digits, numbers and + allowed")
    private String phone;

    @Size(min = 6, max = 50)
    private String password;

    @Size(min = 6, max = 20)
    private String cccdPassport;

    @Past
    private Date dateOfBirth;
}