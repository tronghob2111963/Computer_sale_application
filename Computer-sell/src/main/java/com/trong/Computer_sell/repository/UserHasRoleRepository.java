package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.Role;

import com.trong.Computer_sell.model.UserEntity;
import com.trong.Computer_sell.model.UserHasRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface UserHasRoleRepository extends JpaRepository<UserHasRole, Long> {

    List<String> findByRole(Role role);
    List<String> findByUser(UserEntity user);
}
