package com.vnair.user.service;

import com.vnair.user.dto.Request.role.RoleCreateRequest;
import com.vnair.user.dto.Request.role.RoleUpdateRequest;
import com.vnair.user.dto.Response.role.RolePageResponse;
import com.vnair.user.dto.Response.role.RoleResponse;

import org.springframework.data.domain.Pageable;

public interface RoleService {

    RoleResponse createRole(RoleCreateRequest request);

    RoleResponse updateRole(RoleUpdateRequest request);

    void deleteRole(Integer id);

    RoleResponse getRoleById(Integer id);
    
    RolePageResponse getRoles(Pageable pageable);
}
