package com.vnair.user.dto.Request.user;

import java.util.Date;
import java.util.List;

import com.vnair.user.common.enums.user.UserType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {
    @NotBlank(message = "Full name must not be blank")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Phone must not be blank")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone must be 10-15 digits, only numbers and + allowed")
    private String phone;
    
    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    private String password;
    
    @NotBlank(message = "CCCD/Passport must not be blank")
    @Size(min = 6, max = 20, message = "CCCD/Passport must be between 6 and 20 characters")
    private String cccdPassport;
    
    @Past(message = "Date of birth must be in the past")
    private Date dateOfBirth;
    
    @NotBlank(message = "Username must not be blank")
    private String username;
    
    @NotNull(message = "User type must not be null")
    private UserType userType;
    
    // Keep role system as requested
    private List<Integer> roleIds;
    
    @NotEmpty(message = "Address list must not be empty")
    @Valid
    private List<AddressRequest> addresses;
}
