package com.vnair.user.service.impl;

import com.vnair.user.dto.Request.role.RoleCreateRequest;
import com.vnair.user.dto.Request.role.RoleUpdateRequest;
import com.vnair.user.dto.Response.role.RolePageResponse;
import com.vnair.user.dto.Response.role.RoleResponse;
import com.vnair.user.dto.Response.permission.PermissionResponse;
import com.vnair.user.model.Permission;
import com.vnair.user.model.Role;
import com.vnair.user.model.RoleHasPermission;
import com.vnair.user.repository.PermissionRepository;
import com.vnair.user.repository.RoleRepository;
import com.vnair.user.service.RoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public RoleResponse createRole(RoleCreateRequest request) {
        log.info("Creating role: {}", request.getName());
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());

        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            List<Permission> permissions = permissionRepository.findAllById(request.getPermissionIds());
            for (Permission permission : permissions) {
                RoleHasPermission rp = new RoleHasPermission();
                rp.setRole(role);
                rp.setPermission(permission);
                role.getRolePermissions().add(rp);
            }
        }

        Role savedRole = roleRepository.save(role);
        return toResponse(savedRole);
    }

    @Override
    public RoleResponse updateRole(RoleUpdateRequest request) {
        log.info("Updating role id: {}", request.getId());
        Role role = roleRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        role.setName(request.getName());
        role.setDescription(request.getDescription());

        // Cập nhật permission cho role
        if (request.getPermissionIds() != null) {
            // Xóa các permission cũ
            role.getRolePermissions().clear();

            // Thêm các permission mới
            List<Permission> permissions = permissionRepository.findAllById(request.getPermissionIds());
            for (Permission permission : permissions) {
                RoleHasPermission rp = new RoleHasPermission();
                rp.setRole(role);
                rp.setPermission(permission);
                role.getRolePermissions().add(rp);
            }
        }

        Role updatedRole = roleRepository.save(role);
        return toResponse(updatedRole);
    }

    @Override
    public void deleteRole(Integer id) {
        log.info("Deleting role id: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        roleRepository.delete(role);
    }

    @Override
    public RoleResponse getRoleById(Integer id) {
        log.info("Getting role by id: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return toResponse(role);
    }

    @Override
    public RolePageResponse getRoles(Pageable pageable) {
        log.info("Getting all roles, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Role> page = roleRepository.findAll(pageable);
        RolePageResponse response = new RolePageResponse();
        response.setRoles(page.getContent().stream().map(this::toResponse).collect(Collectors.toList()));
        response.setPageNumber(pageable.getPageNumber());
        response.setPageSize(pageable.getPageSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        return response;
    }

    // Helper: convert Role to RoleResponse (kèm danh sách permission)
    // private RoleResponse toResponse(Role role) {
    //     List<PermissionResponse> permissions = role.getRolePermissions().stream()
    //             .map(rp -> {
    //                 Permission p = rp.getPermission();
    //                 return PermissionResponse.builder()
    //                         .id(p.getId())
    //                         .name(p.getName())
    //                         .description(p.getDescription())
    //                         .build();
    //             })
    //             .collect(Collectors.toList());

    //     return RoleResponse.builder()
    //             .id(role.getId())
    //             .name(role.getName())
    //             .description(role.getDescription())
    //             .permissions(permissions)
    //             .build();
    // }
    private RoleResponse toResponse(Role role) {
        List<PermissionResponse> permissions = role.getRolePermissions().stream()
                .map(rp -> {
                    Permission p = rp.getPermission();
                    return PermissionResponse.builder()
                            .id(p.getId())
                            .resource(p.getResource())
                            .action(p.getAction())
                            .scope(p.getScope())
                            .build();
                })
                .collect(Collectors.toList());

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(permissions)
                .build();
    }
}
