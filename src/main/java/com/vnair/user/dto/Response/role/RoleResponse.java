package com.vnair.user.dto.Response.role;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import com.vnair.user.dto.Response.permission.PermissionResponse;

import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private Integer id;
    private String name;
    private List<PermissionResponse> permissions;
    private String description;
}
