package com.vnair.user.dto.Response.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse implements Serializable {
    private Long id;
    private String street;
    private String city;
    private String country;
    private Integer addressType;
    private String addressTypeName; // For display purpose
}
