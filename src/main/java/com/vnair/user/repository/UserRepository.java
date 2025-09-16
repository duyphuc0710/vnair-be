package com.vnair.user.repository;

import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.vnair.user.model.UserEntity;


@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>{
    @Query("""
    SELECT DISTINCT u FROM UserEntity u
    JOIN FETCH u.userRoles ur
    JOIN FETCH ur.role r
    JOIN FETCH r.rolePermissions rp
    JOIN FETCH rp.permission
    WHERE u.username = :username
    """)
    Optional<UserEntity> findByUsernameWithRolesAndPermissions(String username);

    Optional<UserEntity> findByUsername(String username);
}
