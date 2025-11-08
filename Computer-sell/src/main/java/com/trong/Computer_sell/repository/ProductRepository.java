package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    @Query("SELECT p FROM ProductEntity p WHERE p.name LIKE %:keyword% ")
    Page<ProductEntity> searchUserByKeyword(String keyword, Pageable pageable);


    @Query("SELECT p FROM ProductEntity p WHERE p.brandId.id = :brandId")
    Page<ProductEntity> searchProductByBrandId(UUID brandId, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE p.category.id = :categoryId")
    Page<ProductEntity> searchProductByCategoryId(UUID categoryId, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE p.productTypeId.id = :productTypeId")
    Page<ProductEntity> searchProductByProductTypeId(UUID productTypeId, Pageable pageable);


    @Query("SELECT p FROM ProductEntity p " +
            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND p.productTypeId.id = :productTypeId")
    Page<ProductEntity> searchProductByTypeAndKeyword(UUID productTypeId, String keyword, Pageable pageable);

}
