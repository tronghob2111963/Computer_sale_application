package com.trong.Computer_sell.repository;


import com.trong.Computer_sell.model.ShippingOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ShippingOrderRepository extends JpaRepository<ShippingOrderEntity, UUID> {
}
