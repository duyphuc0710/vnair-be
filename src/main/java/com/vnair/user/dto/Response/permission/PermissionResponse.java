package com.vnair.user.dto.Response.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {
    private Integer id;
    private String resource;  // USER, ORDER, PRODUCT
    private String action;    // READ, CREATE, UPDATE, DELETE
    private String scope;     // OWN, ALL
}
