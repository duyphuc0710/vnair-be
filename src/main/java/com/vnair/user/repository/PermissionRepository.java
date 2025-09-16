package com.vnair.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vnair.user.model.Permission;

@Repository
public interface PermissionRepository  extends JpaRepository<Permission, Integer>{
    
}
