package com.vnair.user.service;

import com.vnair.user.dto.Request.user.UserCreateRequest;
import com.vnair.user.dto.Request.user.UserPasswordRequest;
import com.vnair.user.dto.Request.user.UserUpdateRequest;
import com.vnair.user.dto.Response.user.UserPageResponse;
import com.vnair.user.dto.Response.user.UserResponse;


public interface UserService {
    UserResponse save(UserCreateRequest request);

    void update(Long id, UserUpdateRequest request);

    void changePwd(Long id, UserPasswordRequest request);

    void deleteById(Long id);

    UserPageResponse findAllUsers(int page, int size, String sortBy, String direction, boolean ignoreCase);
    
    UserResponse findById(Long userID);
}
