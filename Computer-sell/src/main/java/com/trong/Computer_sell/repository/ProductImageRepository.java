package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.ProductImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProductImageRepository extends JpaRepository<ProductImageEntity, UUID> {


    @Query("SELECT p.imageUrl FROM ProductImageEntity p WHERE p.product.id = :productId")
    List<String> findProductImageByProductId(UUID productId);


    List<ProductImageEntity> findByProductId(UUID productId);
}
