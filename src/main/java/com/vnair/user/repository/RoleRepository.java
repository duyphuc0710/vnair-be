package com.vnair.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vnair.user.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer>{
    
}
