package com.trong.Computer_sell.model;


import com.trong.Computer_sell.common.Gender;
import com.trong.Computer_sell.common.UserStatus;
import com.trong.Computer_sell.common.UserType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "tbl_users")
public class UserEntity extends AbstractEntity {


    @Column(name="username", unique = true, nullable = false, length = 256)
    private String username;

    @Column(name="password", nullable = false, length = 256)
    private String password;

    @Column(name="first_name", length = 256)
    private String firstName;

    @Column(name="last_name", length = 256)
    private String lastName;


    @Column(name="gender")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name="date_of_birth")
    private LocalDate dateOfBirth;


    @Column(name="phone", length = 15)
    private String phone;


    @Column(name="email", unique = true, nullable = false, length = 256)
    private String email;


    @Column(name="type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Column(name ="status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    private UserStatus status;


}


