package com.vnair.user.dto.Request.user;

import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone must be 10-15 digits, only numbers and + allowed")
    private String phone;
    
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    private String password;
    
    @Size(min = 6, max = 20, message = "CCCD/Passport must be between 6 and 20 characters")
    private String cccdPassport;
    
    @Past(message = "Date of birth must be in the past")
    private Date dateOfBirth;
    
    // Keep role system as requested
    private List<Integer> roleIds;
    
    private List<AddressRequest> addresses;
}
