package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.common.ReviewStatus;
import com.trong.Computer_sell.model.ProductReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReviewEntity, UUID> {

    List<ProductReviewEntity> findByProductIdAndStatusOrderByCreatedAtDesc(UUID productId, ReviewStatus status);

    @Query("""
        SELECT r FROM ProductReviewEntity r
        LEFT JOIN r.product p
        LEFT JOIN r.user u
        WHERE (:productId IS NULL OR p.id = :productId)
          AND (:status IS NULL OR r.status = :status)
          AND (:rating IS NULL OR r.rating = :rating)
          AND (:keywordPattern IS NULL OR
               LOWER(COALESCE(r.comment, '')) LIKE :keywordPattern OR
               LOWER(COALESCE(p.name, '')) LIKE :keywordPattern OR
               LOWER(COALESCE(u.firstName, '')) LIKE :keywordPattern OR
               LOWER(COALESCE(u.lastName, '')) LIKE :keywordPattern)
        """)
    Page<ProductReviewEntity> search(
            @Param("productId") UUID productId,
            @Param("status") ReviewStatus status,
            @Param("rating") Integer rating,
            @Param("keywordPattern") String keywordPattern,
            Pageable pageable
    );

    @Query("""
        SELECT COALESCE(AVG(r.rating), 0) FROM ProductReviewEntity r
        WHERE r.product.id = :productId AND r.status = 'APPROVED'
        """)
    Double getAverageRating(@Param("productId") UUID productId);

    @Query("""
        SELECT r.rating, COUNT(r) FROM ProductReviewEntity r
        WHERE r.product.id = :productId AND r.status = 'APPROVED'
        GROUP BY r.rating
        """)
    List<Object[]> countByRating(@Param("productId") UUID productId);
}
