package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.request.user.EmployeeRequest;
import com.trong.Computer_sell.DTO.response.User.EmployeeResponse;
import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "Quản lý thông tin nhân viên (Employee)")
@Slf4j(topic = "EMPLOYEE-CONTROLLER")
public class EmployeeController {

    private final EmployeeService employeeService;

    // ===================== CREATE =====================
    @Operation(
            summary = "Thêm mới nhân viên",
            description = "Tạo một nhân viên mới trong hệ thống và gắn với tài khoản người dùng.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tạo nhân viên thành công",
                            content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
                    @ApiResponse(responseCode = "404", description = "Không tìm thấy user được liên kết")
            }
    )
    @PostMapping
    @PreAuthorize("hasAnyAuthority('SysAdmin')")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @RequestBody @Parameter(description = "Thông tin nhân viên cần tạo") EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.createEmployee(request));
    }

    // ===================== UPDATE =====================
    @Operation(
            summary = "Cập nhật thông tin nhân viên",
            description = "Cập nhật thông tin vị trí, lương, ghi chú hoặc trạng thái nhân viên.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cập nhật nhân viên thành công",
                            content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên")
            }
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SysAdmin')")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable @Parameter(description = "ID của nhân viên") UUID id,
            @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    // ===================== GET ALL =====================
    @Operation(
            summary = "Lấy danh sách tất cả nhân viên",
            description = "Trả về danh sách toàn bộ nhân viên trong hệ thống.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Thành công",
                            content = @Content(schema = @Schema(implementation = EmployeeResponse.class)))
            }
    )
    @GetMapping
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin')")
    public ResponseEntity<PageResponse<?>> getAllEmployees(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt:desc") String sortBy) {
       try {
           log.info("Fetching employees with keyword: {}", keyword);
           return ResponseEntity.ok(employeeService.findAll(keyword, pageNo, pageSize, sortBy));
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
    }


    // ===================== GET ONE =====================
    @Operation(
            summary = "Lấy thông tin chi tiết một nhân viên",
            description = "Trả về thông tin chi tiết của nhân viên theo ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Thành công",
                            content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên")
            }
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin')")
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @PathVariable @Parameter(description = "ID của nhân viên") UUID id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    // ===================== UPDATE STATUS =====================
    @Operation(
            summary = "Cập nhật trạng thái nhân viên",
            description = "Thay đổi trạng thái của nhân viên (ACTIVE / INACTIVE / ON_LEAVE / RESIGNED).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cập nhật trạng thái thành công"),
                    @ApiResponse(responseCode = "400", description = "Trạng thái không hợp lệ"),
                    @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên")
            }
    )
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin')")
    public ResponseEntity<String> updateStatus(
            @PathVariable @Parameter(description = "ID của nhân viên") UUID id,
            @RequestParam @Parameter(description = "Trạng thái mới (ACTIVE / INACTIVE / ON_LEAVE / RESIGNED)") String status) {
        employeeService.updateEmployeeStatus(id, status);
        return ResponseEntity.ok("Employee status updated successfully");
    }



    @Operation(summary = "Get EmployeeID", description = "Get EmployeeID")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin')")
    public ResponseEntity<UUID> getEmployeeByUserId(
            @PathVariable @Parameter(description = "ID của người dùng") UUID userId) {
        return ResponseEntity.ok(employeeService.getEmployeeIdByUserId(userId));
    }
}
