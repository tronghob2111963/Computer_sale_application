package com.trong.Computer_sell.model;


import com.trong.Computer_sell.common.AddressType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_address")
public class AddressEntity extends AbstractEntity<Long> implements Serializable {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name="apartment_number")
    private String apartmentNumber;

    @Column(name="street_number")
    private String streetNumber;

    @Column(name="ward")
    private String ward;

    @Column(name="city")
    private String city;

    @Column(name = "address_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    private AddressType addressType;



}
