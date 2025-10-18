package com.trong.Computer_sell.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;



@Entity
@Table(name = "tbl_product_types")
@Getter
@Setter
public class ProductTypeEntity extends AbstractEntity implements Serializable {

//    name VARCHAR(255) UNIQUE NOT NULL,
//    description TEXT,
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;
}
