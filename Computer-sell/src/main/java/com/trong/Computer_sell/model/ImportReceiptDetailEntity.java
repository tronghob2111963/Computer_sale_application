package com.trong.Computer_sell.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tbl_import_receipt_details")
@Getter
@Setter
public class ImportReceiptDetailEntity extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id")
    private ImportReceiptEntity receipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    private Integer quantity;
    private BigDecimal importPrice;
    private BigDecimal subtotal;
}