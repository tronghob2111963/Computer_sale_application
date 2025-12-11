package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.ProductVectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVectorRepository extends JpaRepository<ProductVectorEntity, UUID> {

    Optional<ProductVectorEntity> findByProductId(UUID productId);

    @Modifying
    @Query("DELETE FROM ProductVectorEntity pv WHERE pv.productId = :productId")
    void deleteByProductId(@Param("productId") UUID productId);

    @Query(value = """
        SELECT pv.id, pv.product_id, pv.name, pv.price, pv.category, pv.brand, 
               pv.product_type, pv.description, pv.specs, pv.stock, pv.warranty_period,
               pv.created_at, pv.updated_at,
               1 - (pv.embedding <=> CAST(:queryEmbedding AS vector)) as similarity
        FROM product_vectors pv
        WHERE pv.embedding IS NOT NULL
        ORDER BY pv.embedding <=> CAST(:queryEmbedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findSimilarProducts(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("limit") int limit
    );

    @Query(value = """
        SELECT pv.id, pv.product_id, pv.name, pv.price, pv.category, pv.brand,
               pv.product_type, pv.description, pv.specs, pv.stock, pv.warranty_period,
               pv.created_at, pv.updated_at,
               1 - (pv.embedding <=> CAST(:queryEmbedding AS vector)) as similarity
        FROM product_vectors pv
        WHERE pv.embedding IS NOT NULL
          AND pv.price BETWEEN :minPrice AND :maxPrice
        ORDER BY pv.embedding <=> CAST(:queryEmbedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findSimilarProductsWithPriceRange(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("limit") int limit
    );

    @Modifying
    @Query(value = """
        INSERT INTO product_vectors (id, product_id, embedding, name, price, category, brand, 
                                     product_type, description, specs, stock, warranty_period)
        VALUES (:id, :productId, CAST(:embedding AS vector), :name, :price, :category, :brand,
                :productType, :description, :specs, :stock, :warrantyPeriod)
        ON CONFLICT (product_id) DO UPDATE SET
            embedding = CAST(:embedding AS vector),
            name = :name,
            price = :price,
            category = :category,
            brand = :brand,
            product_type = :productType,
            description = :description,
            specs = :specs,
            stock = :stock,
            warranty_period = :warrantyPeriod,
            updated_at = NOW()
        """, nativeQuery = true)
    void upsertProductVector(
            @Param("id") UUID id,
            @Param("productId") UUID productId,
            @Param("embedding") String embedding,
            @Param("name") String name,
            @Param("price") Double price,
            @Param("category") String category,
            @Param("brand") String brand,
            @Param("productType") String productType,
            @Param("description") String description,
            @Param("specs") String specs,
            @Param("stock") Integer stock,
            @Param("warrantyPeriod") Integer warrantyPeriod
    );

    @Query("SELECT COUNT(pv) FROM ProductVectorEntity pv WHERE pv.embedding IS NOT NULL")
    long countWithEmbeddings();
}
