package com.trong.Computer_sell.repository;


import com.trong.Computer_sell.model.ShippingOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShippingOrderRepository extends JpaRepository<ShippingOrderEntity, UUID> {
    
    /**
     * Lấy tất cả phiếu vận chuyển sắp xếp theo thời gian mới nhất
     */
    @Query("SELECT s FROM ShippingOrderEntity s ORDER BY s.createdAt DESC")
    List<ShippingOrderEntity> findAllOrderByCreatedAtDesc();
}
