package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.user.EmployeeRequest;
import com.trong.Computer_sell.DTO.response.User.EmployeeResponse;
import com.trong.Computer_sell.DTO.response.common.PageResponse;

import java.util.List;
import java.util.UUID;

public interface EmployeeService {
    EmployeeResponse createEmployee(EmployeeRequest request);
    EmployeeResponse updateEmployee(UUID id, EmployeeRequest request);
    EmployeeResponse getEmployeeById(UUID id);
    UUID getEmployeeIdByUserId(UUID userId);
    PageResponse<?> findAll(String keyword, int pageNo, int pageSize, String sortBy);
    void updateEmployeeStatus(UUID id, String status);
}
