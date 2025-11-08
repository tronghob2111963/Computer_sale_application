package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.OrderPromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderPromotionRepository extends JpaRepository<OrderPromotionEntity, UUID> {
    List<OrderPromotionEntity> findByOrder_Id(UUID orderId);
    Optional<OrderPromotionEntity> findByOrder_IdAndPromotion_Id(UUID orderId, UUID promotionId);
    void deleteByOrder_Id(UUID orderId);
}