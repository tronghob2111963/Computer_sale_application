package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.ProductTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;


@Repository
public interface ProductTypeRepository extends JpaRepository<ProductTypeEntity, UUID> {

    @Query("SELECT pt.name FROM ProductTypeEntity pt WHERE pt.id = :id")
    String findProductTypeById(UUID id);

    Optional<ProductTypeEntity> findFirstByNameIgnoreCase(String name);
}
