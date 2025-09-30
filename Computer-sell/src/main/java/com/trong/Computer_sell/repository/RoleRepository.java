package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(String name);

    Optional<Object> findById(int roleId);
}
