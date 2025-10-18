package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.UserBuildDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserBuildDetailRepository extends JpaRepository<UserBuildDetailEntity, UUID> {
    List<UserBuildDetailEntity> findByBuildId(UUID buildId);


    UserBuildDetailEntity findByBuildIdAndProductId(UUID buildId, UUID productId);
}
