package com.vnair.user.dto.Request.permission;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionUpdateRequest {
    private Integer id;
    private String resource;  // Ví dụ: USER, ORDER, PRODUCT
    private String action;    // Ví dụ: READ, CREATE, UPDATE, DELETE
    private String scope;     // Ví dụ: ALL, SELF
}
