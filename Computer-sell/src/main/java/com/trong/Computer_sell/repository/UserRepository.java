package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {


    //search user by keyword
    @Query("SELECT u FROM UserEntity u WHERE u.username LIKE %:keyword% OR " +
            "u.email LIKE :keyword OR " +
            "u.phone LIKE :keyword OR " +
            "u.lastName LIKE :keyword OR " +
            "u.firstName LIKE :keyword"
    )
    Page<UserEntity> searchUserByKeyword(String keyword, Pageable pageable);

    UserEntity findByUsername(String username);


    @Query("SELECT u FROM UserEntity u WHERE u.id = :id")
    UserEntity findUserById(UUID id);
}
