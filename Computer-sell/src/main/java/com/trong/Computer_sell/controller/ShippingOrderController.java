package com.trong.Computer_sell.controller;


import com.trong.Computer_sell.DTO.response.Shipping.ShippingOrderResponse;
import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.service.ShippingOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/shipping-orders")
@RequiredArgsConstructor
@Tag(name = "📦 Quản lý phiếu vận chuyển", description = "API cho phép Admin xem, quản lý và xuất phiếu vận chuyển")
public class ShippingOrderController {

    private final ShippingOrderService shippingOrderService;

    // ============================================================
    // 🔹 Lấy tất cả phiếu vận chuyển
    // ============================================================
    @GetMapping
    @Operation(summary = "Lấy danh sách phiếu vận chuyển", description = "Trả về danh sách tất cả phiếu vận chuyển đã được tạo")
    public ResponseEntity<ResponseData<List<ShippingOrderResponse>>> getAllShippingOrders() {
        try {
            List<ShippingOrderResponse> list = shippingOrderService.getAllShippingOrders();
            return ResponseEntity.ok(
                    new ResponseData<>(HttpStatus.OK.value(), "Lấy danh sách phiếu vận chuyển thành công", list)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi khi lấy danh sách phiếu vận chuyển: " + e.getMessage()));
        }
    }

    // ============================================================
    // 🔹 Lấy chi tiết 1 phiếu vận chuyển
    // ============================================================
    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết phiếu vận chuyển", description = "Trả về thông tin chi tiết của 1 phiếu vận chuyển theo ID")
    public ResponseEntity<ResponseData<ShippingOrderResponse>> getShippingOrderById(@PathVariable UUID id) {
        try {
            ShippingOrderResponse response = shippingOrderService.getShippingOrderById(id);
            return ResponseEntity.ok(
                    new ResponseData<>(HttpStatus.OK.value(), "Lấy chi tiết phiếu vận chuyển thành công", response)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseData<>(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi khi lấy phiếu vận chuyển: " + e.getMessage()));
        }
    }

    // ============================================================
    // 🔹 Xuất PDF phiếu vận chuyển
    // ============================================================
    @GetMapping("/{id}/export")
    @Operation(summary = "Xuất phiếu vận chuyển ra PDF", description = "Xuất file PDF chứa thông tin giao hàng để in hoặc gửi cho đơn vị vận chuyển")
    public ResponseEntity<?> exportShippingOrderToPdf(@PathVariable UUID id) {
        try {
            ByteArrayInputStream pdfStream = shippingOrderService.exportShippingOrderToPdf(id);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=shipping-order-" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfStream.readAllBytes());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseData<>(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi khi xuất PDF: " + e.getMessage()));
        }
    }
}