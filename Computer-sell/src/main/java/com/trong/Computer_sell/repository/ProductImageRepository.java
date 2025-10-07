package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.ProductImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductImageRepository extends JpaRepository<ProductImageEntity, UUID> {
}
