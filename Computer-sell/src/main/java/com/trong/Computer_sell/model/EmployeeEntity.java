package com.trong.Computer_sell.model;

import com.trong.Computer_sell.common.EmployeeStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "tbl_employees")
@Getter
@Setter
public class EmployeeEntity extends AbstractEntity implements Serializable {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String position;

    private BigDecimal salary;

    private LocalDate hireDate;

    private LocalDate terminateDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String note;
}
