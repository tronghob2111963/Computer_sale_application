package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.request.user.EmployeeRequest;
import com.trong.Computer_sell.DTO.response.User.EmployeeResponse;
import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.common.EmployeeStatus;
import com.trong.Computer_sell.model.EmployeeEntity;
import com.trong.Computer_sell.model.UserEntity;
import com.trong.Computer_sell.repository.EmployeeRepository;
import com.trong.Computer_sell.repository.UserRepository;
import com.trong.Computer_sell.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "EMPLOYEE-SERVICE")
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        EmployeeEntity employee = new EmployeeEntity();
        employee.setUser(user);
        employee.setPosition(request.getPosition());
        employee.setSalary(request.getSalary());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(EmployeeStatus.ACTIVE);

        employeeRepository.save(employee);
        return EmployeeResponse.fromEntity(employee);
    }

    @Override
    public EmployeeResponse updateEmployee(UUID id, EmployeeRequest request) {
        EmployeeEntity employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setPosition(request.getPosition());
        employee.setSalary(request.getSalary());
        employee.setNote(request.getNote());
        employee.setStatus(request.getStatus());
    return EmployeeResponse.fromEntity(employee);
    }

    @Override
    public EmployeeResponse getEmployeeById(UUID id) {
        return employeeRepository.findById(id)
                .map(EmployeeResponse::fromEntity)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    @Override
    public PageResponse<?> findAll(String keyword, int pageNo, int pageSize, String sortBy) {
        log.info("Fetching employees with keyword: {}", keyword);

        int pageIndex = pageNo > 0 ? pageNo - 1 : 0;
        List<Sort.Order> sortOrders = new java.util.ArrayList<>();

        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(asc|desc)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                Sort.Direction direction = matcher.group(3).equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC : Sort.Direction.DESC;
                sortOrders.add(new Sort.Order(direction, matcher.group(1)));
            }
        } else {
            sortOrders.add(new Sort.Order(Sort.Direction.ASC, "createdAt"));
        }

        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(sortOrders));
        Page<EmployeeEntity> employeePage;

        if (StringUtils.hasLength(keyword)) {
            employeePage = employeeRepository.searchEmployees(keyword, pageable);
        } else {
            employeePage = employeeRepository.findAll(pageable);
        }

        List<EmployeeResponse> employeeList = employeePage.stream()
                .map(EmployeeResponse::fromEntity)
                .toList();

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(employeePage.getTotalElements())
                .totalPages(employeePage.getTotalPages())
                .items(employeeList)
                .build();
    }

    @Override
    public void updateEmployeeStatus(UUID id, String status) {
        EmployeeEntity employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setStatus(EmployeeStatus.valueOf(status));
        employeeRepository.save(employee);
    }

    @Override
    public UUID getEmployeeIdByUserId(UUID userId) {
        try{
            return employeeRepository.getEmployeeIdByUserId(userId);
        }catch (Exception e){
            throw new RuntimeException("User not found");
        }
    }
}