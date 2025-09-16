package com.vnair.user.controller;

import com.vnair.user.dto.Request.permission.PermissionCreateResquest;
import com.vnair.user.dto.Request.permission.PermissionUpdateRequest;
import com.vnair.user.dto.Response.permission.PermissionPageReponse;
import com.vnair.user.dto.Response.permission.PermissionResponse;
import com.vnair.user.service.PermissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission Controller", description = "API for managing permissions")
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @Operation(summary = "Create a new permission")
    public PermissionResponse create(@RequestBody PermissionCreateResquest request) {
        log.info("Create permission: {}:{}:{}", request.getResource(), request.getAction(), request.getScope());
        return permissionService.save(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get permission by id")
    public PermissionResponse getById(@PathVariable Integer id) {
        log.info("Get permission by id: {}", id);
        return permissionService.findById(id);
    }

    @GetMapping
    @Operation(summary = "Get all permissions")
    public PermissionPageReponse getAll(Pageable pageable) {
        log.info("Get all permissions, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return permissionService.finđAll(pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update permission")
    public PermissionResponse update(@PathVariable Integer id, @RequestBody PermissionUpdateRequest request) {
        log.info("Update permission id: {}", id);
        request.setId(id);
        return permissionService.update(request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete permission")
    public void delete(@PathVariable Integer id) {
        log.info("Delete permission id: {}", id);
        permissionService.delete(id);
    }
}
