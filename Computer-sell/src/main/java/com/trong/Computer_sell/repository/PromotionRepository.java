package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.PromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;



import com.trong.Computer_sell.model.PromotionEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface PromotionRepository extends JpaRepository<PromotionEntity, UUID> {

    Optional<PromotionEntity> findByPromoCode(String promoCode);

    @Query("""
        SELECT p FROM PromotionEntity p
        WHERE p.isActive = true
        AND :now BETWEEN p.startDate AND p.endDate
    """)
    List<PromotionEntity> findActivePromotions(@Param("now") LocalDateTime now);

    @Query("""
        SELECT p FROM PromotionEntity p
        WHERE p.promoCode = :code
        AND p.isActive = true
        AND :now BETWEEN p.startDate AND p.endDate
    """)
    Optional<PromotionEntity> findValidPromotionByCode(@Param("code") String code, @Param("now") LocalDateTime now);
}
