package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(String name);

    Optional<Object> findById(int roleId);

    @Query("SELECT r.name FROM Role r WHERE r.id = :roleId")
    String findRoleNameById(int roleId);
}
