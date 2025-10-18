package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<CartEntity, UUID> {

    // Một user chỉ có 1 giỏ hàng ACTIVE tại 1 thời điểm
    @Query("SELECT c FROM CartEntity c WHERE c.user.id = :userId AND c.status = 'ACTIVE'")
    Optional<CartEntity> findActiveCartByUserId(@Param("userId") UUID userId);
}
