package com.vnair.user.controller;

import com.vnair.user.dto.Request.role.RoleCreateRequest;
import com.vnair.user.dto.Request.role.RoleUpdateRequest;
import com.vnair.user.dto.Response.role.RolePageResponse;
import com.vnair.user.dto.Response.role.RoleResponse;
import com.vnair.user.service.RoleService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Role Controller", description = "API for managing roles")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @Operation(summary = "Create a new role")
    public RoleResponse create(@RequestBody RoleCreateRequest request) {
        log.info("Create role: {}", request.getName());
        return roleService.createRole(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by id")
    public RoleResponse getById(@PathVariable Integer id) {
        log.info("Get role by id: {}", id);
        return roleService.getRoleById(id);
    }

    @GetMapping
    @Operation(summary = "Get all roles")
    public RolePageResponse getAll(Pageable pageable) {
        log.info("Get all roles, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return roleService.getRoles(pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update role")
    public RoleResponse update(@PathVariable Integer id, @RequestBody RoleUpdateRequest request) {
        log.info("Update role id: {}", id);
        request.setId(id);
        return roleService.updateRole(request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role")
    public void delete(@PathVariable Integer id) {
        log.info("Delete role id: {}", id);
        roleService.deleteRole(id);
    }
}
