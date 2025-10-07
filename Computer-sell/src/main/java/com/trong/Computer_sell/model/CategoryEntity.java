package com.trong.Computer_sell.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Entity
@Table(name = "tbl_categories")
@Getter
@Setter
public class CategoryEntity extends AbstractEntity implements Serializable {

    @Column(nullable = false)
    private String name;

    private String description;
}
