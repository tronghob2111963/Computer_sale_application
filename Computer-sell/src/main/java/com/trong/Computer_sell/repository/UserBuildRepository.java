package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.UserBuildEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserBuildRepository extends JpaRepository<UserBuildEntity, UUID> {
    List<UserBuildEntity> findByUserId(UUID userId);


    @Query("SELECT u FROM UserBuildEntity u WHERE u.name LIKE %:keyword%")
    Page<UserBuildEntity> searchBuildByKeyword(String keyword, Pageable pageable);
}
