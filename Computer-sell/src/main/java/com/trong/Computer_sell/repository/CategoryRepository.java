package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {


    @Query("SELECT c.name FROM CategoryEntity c WHERE c.id = :id")
    String findCategoryNameById(UUID id);

    @Query("SELECT c FROM CategoryEntity c WHERE c.name LIKE %:keyword% ")
    Page<CategoryEntity> searchCategoryByKeyword(String keyword, Pageable pageable);
}
