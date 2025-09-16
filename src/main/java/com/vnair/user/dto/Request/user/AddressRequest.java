package com.vnair.user.dto.Request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {
    @NotBlank(message = "Street must not be blank")
    private String street;
    
    @NotBlank(message = "City must not be blank")
    private String city;
    
    @NotBlank(message = "Country must not be blank")
    private String country;
    
    @NotNull(message = "Address type must not be null")
    private Integer addressType;
}
