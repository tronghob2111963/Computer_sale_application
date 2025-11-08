package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.OrderDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetailEntity, UUID> {
    List<OrderDetailEntity> findByOrder_Id(UUID orderId);
    void deleteByOrder_Id(UUID orderId);
}