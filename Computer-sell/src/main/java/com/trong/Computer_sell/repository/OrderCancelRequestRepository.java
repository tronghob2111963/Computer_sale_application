package com.trong.Computer_sell.repository;


import com.trong.Computer_sell.model.OrderCancelRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderCancelRequestRepository extends JpaRepository<OrderCancelRequestEntity, UUID> {
    Optional<OrderCancelRequestEntity> findByOrder_Id(UUID orderId);
}
