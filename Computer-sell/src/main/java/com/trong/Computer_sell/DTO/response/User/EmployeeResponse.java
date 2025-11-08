package com.trong.Computer_sell.DTO.response.User;

import com.trong.Computer_sell.common.EmployeeStatus;
import com.trong.Computer_sell.model.EmployeeEntity;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class EmployeeResponse {
    private UUID id;
    private UUID userId;
    private String username;
    private String position;
    private BigDecimal salary;
    private LocalDate hireDate;
    private LocalDate terminateDate;
    private EmployeeStatus status;
    private String note;

    public static EmployeeResponse fromEntity(EmployeeEntity e) {
        return EmployeeResponse.builder()
                .id(e.getId())
                .userId(e.getUser().getId())
                .username(e.getUser().getUsername())
                .position(e.getPosition())
                .salary(e.getSalary())
                .hireDate(e.getHireDate())
                .terminateDate(e.getTerminateDate())
                .status(e.getStatus())
                .note(e.getNote())
                .build();
    }
}