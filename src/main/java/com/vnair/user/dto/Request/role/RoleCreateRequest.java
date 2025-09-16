package com.vnair.user.dto.Request.role;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleCreateRequest {
    private String name;
    private List<Integer> permissionIds;
    private String description;
}
