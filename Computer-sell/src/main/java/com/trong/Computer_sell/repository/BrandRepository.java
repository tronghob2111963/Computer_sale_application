package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.BrandEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BrandRepository extends JpaRepository<BrandEntity, Long> {
    List<BrandEntity> id(UUID id);

    @Query("SELECT b FROM BrandEntity b WHERE b.id = :id")
    BrandEntity findBrandById(UUID id);

    @Query("SELECT b FROM BrandEntity b WHERE b.name LIKE %:keyword% OR " +
            "b.country LIKE :keyword "
           )
    Page<BrandEntity> searchBranchByKeyword(String keyword, Pageable pageable);


    @Query("SELECT b.name FROM BrandEntity b WHERE b.id = :id")
    String findBrandNameById(UUID id);
}
