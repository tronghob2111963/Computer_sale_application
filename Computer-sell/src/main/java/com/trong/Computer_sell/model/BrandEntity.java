package com.trong.Computer_sell.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "tbl_brands")
@Getter
@Setter
public class BrandEntity extends AbstractEntity implements Serializable {

    @Column(nullable = false)
    private String name;
    private String country;
    private String description;

}
