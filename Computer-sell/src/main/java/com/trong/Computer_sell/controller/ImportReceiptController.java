package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.request.product.ImportReceiptRequest;
import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.DTO.response.product.ImportReceiptResponse;
import com.trong.Computer_sell.service.ImportReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/import-receipts")
@RequiredArgsConstructor
@Tag(name = "Import Receipt", description = "API quản lý phiếu nhập hàng")
public class ImportReceiptController {

    private final ImportReceiptService importReceiptService;


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
}
