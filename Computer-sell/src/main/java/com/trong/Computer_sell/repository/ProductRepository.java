package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
}
