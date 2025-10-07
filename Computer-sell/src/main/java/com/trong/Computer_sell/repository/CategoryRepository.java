package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {


    @Query("SELECT c.name FROM CategoryEntity c WHERE c.id = :id")
    String findCategoryNameById(UUID id);
}
