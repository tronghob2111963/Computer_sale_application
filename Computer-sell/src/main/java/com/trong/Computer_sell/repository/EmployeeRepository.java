package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.EmployeeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {
    @Query("""
        SELECT e FROM EmployeeEntity e
        JOIN e.user u
        WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(e.position) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<EmployeeEntity> searchEmployees(String keyword, Pageable pageable);


    @Query("SELECT e.id FROM EmployeeEntity e WHERE e.user.id = :userId")
    UUID getEmployeeIdByUserId(UUID userId);
}
