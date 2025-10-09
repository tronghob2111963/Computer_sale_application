package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.ProductEntity;
import com.trong.Computer_sell.model.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    @Query("SELECT p FROM ProductEntity p WHERE p.name LIKE %:keyword% ")
    Page<ProductEntity> searchUserByKeyword(String keyword, Pageable pageable);
}
