package com.vnair.user.service.impl;

import com.vnair.common.exception.ResourceNotFoundException;
import com.vnair.user.dto.Request.permission.PermissionCreateResquest;
import com.vnair.user.dto.Request.permission.PermissionUpdateRequest;
import com.vnair.user.dto.Response.permission.PermissionPageReponse;
import com.vnair.user.dto.Response.permission.PermissionResponse;
import com.vnair.user.model.Permission;
import com.vnair.user.repository.PermissionRepository;
import com.vnair.user.service.PermissionService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Tag(name = "Permission Service", description = "Service for managing permissions")
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    public PermissionResponse save(PermissionCreateResquest request) {
        log.info("Saving new permission: {}:{}:{}", request.getResource(), request.getAction(), request.getScope());
        Permission permission = Permission.builder()
                .resource(request.getResource())
                .action(request.getAction())
                .scope(request.getScope())
                .build();
        Permission saved = permissionRepository.save(permission);
        log.info("Permission saved with id: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public PermissionResponse findById(Integer id) {
        log.info("Finding permission by id: {}", id);
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Permission not found with id: {}", id);
                    return new ResourceNotFoundException("Permission not found");
                });
        return toResponse(permission);
    }

    @Override
    public PermissionPageReponse finđAll(Pageable pageable) {
        log.info("Fetching all permissions, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Permission> page = permissionRepository.findAll(pageable);
        PermissionPageReponse response = new PermissionPageReponse();
        response.setPermissions(page.getContent().stream().map(this::toResponse).collect(Collectors.toList()));
        response.setPageNumber(pageable.getPageNumber());
        response.setPageSize(pageable.getPageSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        return response;
    }

    @Override
    public PermissionResponse update(PermissionUpdateRequest request) {
        log.info("Updating permission id: {}", request.getId());
        Permission permission = permissionRepository.findById(request.getId())
                .orElseThrow(() -> {
                    log.error("Permission not found with id: {}", request.getId());
                    return new ResourceNotFoundException("Permission not found");
                });
        permission.setResource(request.getResource());
        permission.setAction(request.getAction());
        permission.setScope(request.getScope());
        Permission updated = permissionRepository.save(permission);
        log.info("Permission updated with id: {}", updated.getId());
        return toResponse(updated);
    }

    @Override
    public void delete(Integer id) {
        log.info("Deleting permission id: {}", id);
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Permission not found with id: {}", id);
                    return new ResourceNotFoundException("Permission not found");
                });
        permissionRepository.delete(permission);
        log.info("Permission deleted with id: {}", id);
    }

    // Helper method
    private PermissionResponse toResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .resource(permission.getResource())
                .action(permission.getAction())
                .scope(permission.getScope())
                .build();
    }
}
