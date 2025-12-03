package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.request.product.ImportReceiptRequest;
import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.DTO.response.product.ImportReceiptResponse;
import com.trong.Computer_sell.common.ImportReceiptStatus;
import com.trong.Computer_sell.model.UserEntity;
import com.trong.Computer_sell.service.EmployeeService;
import com.trong.Computer_sell.service.ImportReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/import-receipts")
@RequiredArgsConstructor
@Tag(name = "Import Receipt", description = "API quản lý phiếu nhập hàng")
public class ImportReceiptController {

    private final ImportReceiptService importReceiptService;
    private final EmployeeService employeeService;


    //  Tạo phiếu nhập hàng
    @Operation(
            summary = "Tạo phiếu nhập hàng mới",
            description = "Dành cho Admin / Nhân viên nhập hàng",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Tạo phiếu nhập thành công",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ImportReceiptResponse.class),
                                    examples = @ExampleObject(value = """
                                    {
                                      "employeeId": "bcd8a6a4-6571-4a1c-b61e-5ffdf731e123",
                                      "note": "Nhập hàng linh kiện đợt tháng 10",
                                      "items": [
                                        {
                                          "productId": "a1b2c3d4-e5f6-7a8b-9c0d-ef1234567890",
                                          "quantity": 5,
                                          "importPrice": 3500000
                                        },
                                        {
                                          "productId": "b2c3d4e5-f6a7-8b9c-0d1e-234567890abc",
                                          "quantity": 10,
                                          "importPrice": 1200000
                                        }
                                      ]
                                    }
                                    """)))
            }
    )
    @PostMapping
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin','Staff')")
    public ResponseEntity<?> createImportReceipt(@RequestBody ImportReceiptRequest request) {
        try {
            // Tự động lấy employeeId từ user đang đăng nhập nếu không được truyền vào
            if (request.getEmployeeId() == null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof UserEntity) {
                    UserEntity currentUser = (UserEntity) auth.getPrincipal();
                    UUID employeeId = employeeService.getEmployeeIdByUserId(currentUser.getId());
                    request.setEmployeeId(employeeId);
                }
            }

            ImportReceiptResponse response = importReceiptService.createImportReceipt(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ResponseData<>(HttpStatus.CREATED.value(), "Import receipt created successfully", response));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    // API lấy employeeId của user đang đăng nhập
    @Operation(summary = "Lấy employeeId của user đang đăng nhập")
    @GetMapping("/current-employee")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin','Staff')")
    public ResponseEntity<?> getCurrentEmployeeId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserEntity) {
                UserEntity currentUser = (UserEntity) auth.getPrincipal();
                UUID employeeId = employeeService.getEmployeeIdByUserId(currentUser.getId());
                return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Employee ID found", employeeId));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseError(HttpStatus.UNAUTHORIZED.value(), "User not authenticated"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    // 2. Lấy danh sách phiếu nhập hàng
    @Operation(summary = "Danh sách tất cả phiếu nhập hàng",
            description = "Dành cho Admin / Nhân viên kho")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin','Staff')")
    public ResponseEntity<?> getAllImportReceipts() {
        try {
            List<ImportReceiptResponse> receipts = importReceiptService.getAllImportReceipts();
            return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Import receipts retrieved successfully", receipts));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    //  3. Xem chi tiết 1 phiếu nhập hàng
    @Operation(summary = "Xem chi tiết phiếu nhập hàng theo ID",
            description = "Dành cho Admin / Nhân viên kho")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin','Staff')")
    public ResponseEntity<?> getImportReceiptById(@PathVariable UUID id) {
        try {
            ImportReceiptResponse response = importReceiptService.getImportReceiptById(id);
            return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Import receipt found successfully", response));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    // 4. Hủy phiếu nhập hàng (hoàn lại kho)
    @Operation(summary = "Hủy phiếu nhập hàng",
            description = "Hủy phiếu nhập và hoàn lại số lượng vào kho")
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin')")
    public ResponseEntity<?> cancelImportReceipt(
            @PathVariable UUID id,
            @RequestParam String cancelledBy) {
        try {
            importReceiptService.cancelImportReceipt(id, cancelledBy);
            return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Import receipt cancelled successfully", null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    // 5. Lọc phiếu nhập theo trạng thái
    @Operation(summary = "Lọc phiếu nhập theo trạng thái",
            description = "Lọc theo PENDING, COMPLETED, CANCELLED")
    @GetMapping("/filter/status/{status}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin','Staff')")
    public ResponseEntity<?> getImportReceiptsByStatus(@PathVariable ImportReceiptStatus status) {
        try {
            List<ImportReceiptResponse> receipts = importReceiptService.getImportReceiptsByStatus(status);
            return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Import receipts retrieved", receipts));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    // 6. Lọc phiếu nhập theo khoảng thời gian
    @Operation(summary = "Lọc phiếu nhập theo khoảng thời gian")
    @GetMapping("/filter/date-range")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin','Staff')")
    public ResponseEntity<?> getImportReceiptsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<ImportReceiptResponse> receipts = importReceiptService.getImportReceiptsByDateRange(start, end);
            return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Import receipts retrieved", receipts));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    // 7. Lọc phiếu nhập theo nhân viên
    @Operation(summary = "Lọc phiếu nhập theo nhân viên")
    @GetMapping("/filter/employee/{employeeId}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin','Staff')")
    public ResponseEntity<?> getImportReceiptsByEmployee(@PathVariable UUID employeeId) {
        try {
            List<ImportReceiptResponse> receipts = importReceiptService.getImportReceiptsByEmployee(employeeId);
            return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Import receipts retrieved", receipts));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }
}
