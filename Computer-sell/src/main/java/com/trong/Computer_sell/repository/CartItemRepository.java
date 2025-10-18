package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItemEntity, UUID> {
    Optional<CartItemEntity> findByCartIdAndProductId(UUID cartId, UUID productId);
    List<CartItemEntity> findByCartId(UUID cartId);
}
