package com.trong.Computer_sell.model;


import com.trong.Computer_sell.common.Gender;
import com.trong.Computer_sell.common.UserStatus;
import com.trong.Computer_sell.common.UserType;
import com.trong.Computer_sell.service.UserServiceDetail;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "tbl_users")
public class UserEntity implements UserDetails, Serializable {



    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;




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

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<UserHasRole> roles = new HashSet<>();



    @Column(name="created_at")
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private LocalDateTime updatedAt = LocalDateTime.now();


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //1.get role list
        List<Role> roleList =roles.stream().map(UserHasRole::getRole).toList();

        //2.get role name
        List<String> roleNames = roleList.stream().map(Role::getName).toList();

        //3.Add role name to authorities
        return roleNames.stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserStatus.ACTIVE.equals(status);
    }
}


