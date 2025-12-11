package com.trong.Computer_sell.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_vectors")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVectorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false, unique = true)
    private UUID productId;

    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private String embedding;

    @Column(name = "name")
    private String name;

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "category")
    private String category;

    @Column(name = "brand")
    private String brand;

    @Column(name = "product_type")
    private String productType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "specs", columnDefinition = "TEXT")
    private String specs;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "warranty_period")
    private Integer warrantyPeriod;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
