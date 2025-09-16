package com.vnair.user.service.impl;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vnair.common.exception.ResourceNotFoundException;
import com.vnair.user.common.enums.user.UserStatus;
import com.vnair.user.dto.Request.user.AddressRequest;
import com.vnair.user.dto.Request.user.UserCreateRequest;
import com.vnair.user.dto.Request.user.UserPasswordRequest;
import com.vnair.user.dto.Request.user.UserUpdateRequest;
import com.vnair.user.dto.Response.user.UserPageResponse;
import com.vnair.user.dto.Response.user.UserResponse;
import com.vnair.user.model.AddressEntity;
import com.vnair.user.model.Role;
import com.vnair.user.model.UserEntity;
import com.vnair.user.model.UserHasRole;
import com.vnair.user.repository.RoleRepository;
import com.vnair.user.repository.UserRepository;
import com.vnair.user.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j(topic = "USER-SERVICE")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse save(UserCreateRequest req) {
        log.info("Saving user: {}", req);

        // Build user entity
        UserEntity user = buildUserEntity(req);
        log.info("Built UserEntity: {}", user);

        // Add addresses
        addAddressesToUser(user, req.getAddresses());
        log.info("Added addresses to user: {}", user.getAddresses());

        // Gán role cho user thông qua UserHasRole
        if (req.getRoleIds() != null && !req.getRoleIds().isEmpty()) {
            List<Role> roles = roleRepository.findAllById(req.getRoleIds());
            for (Role role : roles) {
                UserHasRole userHasRole = new UserHasRole();
                userHasRole.setUser(user);
                userHasRole.setRole(role);
                user.getUserRoles().add(userHasRole);
            }
            log.info("Assigned roles to user: {}", req.getRoleIds());
        }

        userRepository.save(user);
        log.info("Saved user successfully: {}", user);

        return mapToUserResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, UserUpdateRequest req) {
        log.info("Updating user id: {}, request: {}", id, req);

        UserEntity user = getUserEntity(id);
        updateUserEntity(user, req);

        if (req.getAddresses() != null) {
            user.getAddresses().clear();
            addAddressesToUser(user, req.getAddresses());
            log.info("Updated addresses for user: {}", user.getAddresses());
        }

        // Cập nhật role cho user thông qua UserHasRole
        if (req.getRoleIds() != null) {
            user.getUserRoles().clear();
            List<Role> roles = roleRepository.findAllById(req.getRoleIds());
            for (Role role : roles) {
                UserHasRole userHasRole = new UserHasRole();
                userHasRole.setUser(user);
                userHasRole.setRole(role);
                user.getUserRoles().add(userHasRole);
            }
            log.info("Updated roles for user: {}", req.getRoleIds());
        }

        userRepository.save(user);

        log.info("Updated user successful id: {}", id);
    }

    @Override
    public void changePwd(Long id, UserPasswordRequest req) {
        log.info("Changing password for user id: {}, request: {}", id, req);

        UserEntity user = getUserEntity(id);
        if (req.getNewPassword().equals(req.getConfirmPassword())) {
            user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        }

        userRepository.save(user);
        log.info("Password changed successfully for user id: {}", id);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Deleting user: {}", id);

        UserEntity user = getUserEntity(id);
        user.setStatus(UserStatus.INACTIVE);

        userRepository.save(user);
        log.info("Deleted user id: {}", id);
    }

    @Override
    public UserPageResponse findAllUsers(int page, int size, String sortBy, String direction, boolean ignoreCase) {
        log.info("Get all users");

        Pageable pageable = buildPageable(page, size, sortBy, direction, ignoreCase);

        Page<UserEntity> userEntities = userRepository.findAll(pageable);

        List<UserResponse> userList = userEntities.stream()
            .map(this::mapToUserResponse)
            .toList();

        UserPageResponse response = new UserPageResponse();
        response.setPageNumber(pageable.getPageNumber());
        response.setPageSize(pageable.getPageSize());
        response.setTotalElements(userEntities.getTotalElements());
        response.setTotalPages(userEntities.getTotalPages());
        response.setUsers(userList);

        return response;
    }

    @Override
    public UserResponse findById(Long id) {
        log.info("Find user by id: {}", id);

        UserEntity userEntity = getUserEntity(id);
        return mapToUserResponse(userEntity);
    }

    // method helper
    private UserEntity getUserEntity(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserEntity buildUserEntity(UserCreateRequest req) {
        UserEntity user = new UserEntity();
        user.setFullName(req.getFullName());
        user.setDateOfBirth(req.getDateOfBirth());
        user.setPhone(req.getPhone());
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        user.setCccdPassport(req.getCccdPassport());
        user.setUserType(req.getUserType());
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private void updateUserEntity(UserEntity user, UserUpdateRequest req) {
        if (req.getFullName() != null) {
            user.setFullName(req.getFullName());
        }
        if (req.getDateOfBirth() != null) {
            user.setDateOfBirth(req.getDateOfBirth());
        }
        if (req.getPhone() != null) {
            user.setPhone(req.getPhone());
        }
        if (req.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        if (req.getEmail() != null) {
            user.setEmail(req.getEmail());
        }
        if (req.getCccdPassport() != null) {
            user.setCccdPassport(req.getCccdPassport());
        }
    }

    private void addAddressesToUser(UserEntity user, List<AddressRequest> addressRequests) {
        if (addressRequests == null) return;
        for (AddressRequest a : addressRequests) {
            AddressEntity address = new AddressEntity();
            address.setStreet(a.getStreet());
            address.setCity(a.getCity());
            address.setAddressType(a.getAddressType());
            user.saveAddress(address);
        }
    }

    private UserResponse mapToUserResponse(UserEntity entity) {
        return UserResponse.builder()
            .id(entity.getId())
            .fullName(entity.getFullName())
            .dateOfBirth(entity.getDateOfBirth())
            .username(entity.getUsername())
            .phone(entity.getPhone())
            .email(entity.getEmail())
            .cccdPassport(entity.getCccdPassport())
            .status(entity.getStatus())
            .userType(entity.getUserType())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedBy(entity.getUpdatedBy())
            .build();
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction, boolean ignoreCase) {
        Sort.Order order = new Sort.Order(Sort.Direction.fromString(direction), sortBy);
        if (ignoreCase) order = order.ignoreCase();
        Sort sort = Sort.by(order);
        return PageRequest.of(page, size, sort);
    }
}
