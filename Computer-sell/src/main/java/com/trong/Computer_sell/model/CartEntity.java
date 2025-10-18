package com.trong.Computer_sell.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;



@Entity
@Table(name = "tbl_cart")
@Getter
@Setter
public class CartEntity extends AbstractEntity implements Serializable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "total_price")
    private BigDecimal totalPrice = BigDecimal.ZERO;

    private String status; // ACTIVE, CHECKED_OUT, ABANDONED

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItemEntity> items = new ArrayList<>();
}

