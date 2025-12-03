package com.trong.Computer_sell.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trong.Computer_sell.common.ProductStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_products")
@Getter
@Setter
public class ProductEntity extends AbstractEntity implements Serializable {
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock = 0; // Mặc định tồn kho = 0 khi tạo mới

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", referencedColumnName = "id")
    private BrandEntity brandId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id", referencedColumnName = "id")
    private ProductTypeEntity productTypeId;

    @Column(name = "warranty_period")
    private Integer warrantyPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ProductStatus status = ProductStatus.ACTIVE; // Trạng thái sản phẩm

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ProductImageEntity> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<StockHistoryEntity> stockHistories = new ArrayList<>();
}
