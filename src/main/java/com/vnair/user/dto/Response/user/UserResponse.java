package com.vnair.user.dto.Response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vnair.user.common.enums.user.UserStatus;
import com.vnair.user.common.enums.user.UserType;
import com.vnair.user.dto.Response.address.AddressResponse;
import com.vnair.user.dto.Response.role.RoleResponse;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse implements Serializable {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String cccdPassport;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateOfBirth;
    
    private UserStatus status;
    private UserType userType;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;
    
    private String createdBy;
    private String updatedBy;
    
    // Nested objects
    private List<AddressResponse> addresses;
    private Set<RoleResponse> roles;
}
