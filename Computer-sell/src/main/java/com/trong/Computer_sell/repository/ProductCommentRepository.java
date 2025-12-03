package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.common.CommentStatus;
import com.trong.Computer_sell.model.ProductCommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductCommentRepository extends JpaRepository<ProductCommentEntity, UUID> {

    List<ProductCommentEntity> findByProduct_IdAndStatusOrderByCreatedAtAsc(UUID productId, CommentStatus status);

    @Query("""
            SELECT c FROM ProductCommentEntity c
            WHERE (:productId IS NULL OR c.product.id = :productId)
              AND (:status IS NULL OR c.status = :status)
              AND (:keyword IS NULL OR LOWER(COALESCE(c.content, '')) LIKE :keyword)
            """)
    Page<ProductCommentEntity> search(
            @Param("productId") UUID productId,
            @Param("status") CommentStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
