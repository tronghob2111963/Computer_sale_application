package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {


    //search user by keyword
    @Query("""
        SELECT u FROM UserEntity u
        WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<UserEntity> searchUserByKeyword(@Param("keyword") String keyword, Pageable pageable);


    @Query("SELECT u FROM UserEntity u WHERE u.username = :username")
    UserEntity findByUsername(String username);

    @Query("SELECT u FROM UserEntity u WHERE u.email = :email")
    UserEntity findByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE u.phone = :phone")
    UserEntity findByPhone(String phone);

    @Query("SELECT u FROM UserEntity u WHERE u.id = :id")
    UserEntity findUserById(UUID id);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);



}
