package com.trong.Computer_sell.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Entity
@Table(name = "tbl_user_build_details")
@Getter
@Setter
public class UserBuildDetailEntity extends AbstractEntity implements Serializable {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_id")
    @JsonIgnore
    private UserBuildEntity build;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    private Integer quantity = 1;
}
