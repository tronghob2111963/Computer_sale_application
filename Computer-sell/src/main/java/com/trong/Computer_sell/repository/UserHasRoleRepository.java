package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.Role;

import com.trong.Computer_sell.model.UserEntity;
import com.trong.Computer_sell.model.UserHasRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;


public interface UserHasRoleRepository extends JpaRepository<UserHasRole, Long> {

    List<String> findByRole(Role role);
    List<String> findByUser(UserEntity user);

    // Lấy danh sách user ID theo tên role
    @Query("SELECT uhr.user.id FROM UserHasRole uhr WHERE uhr.role.name = :roleName")
    List<UUID> findUserIdsByRoleName(@Param("roleName") String roleName);
}
