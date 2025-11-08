package com.trong.Computer_sell.DTO.request.user;

import com.trong.Computer_sell.common.EmployeeStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class EmployeeRequest {
    private UUID userId;
    private String position;
    private BigDecimal salary;
    private LocalDate hireDate;
    private LocalDate terminateDate;
    private String note;
    private EmployeeStatus status;
}
