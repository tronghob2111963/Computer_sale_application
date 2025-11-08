package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.response.oder.OrderResponse;
import com.trong.Computer_sell.common.OrderStatus;
import com.trong.Computer_sell.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('SysAdmin','Admin','Staff')")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<?> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        var data = adminOrderService.filterOrders(status, start, end);
        return ResponseEntity.ok(new com.trong.Computer_sell.DTO.response.common.ResponseData<>(200, "Success", data));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable UUID id,
            @RequestParam OrderStatus status) {
        adminOrderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(new com.trong.Computer_sell.DTO.response.common.ResponseData<>(200, "Order status updated successfully"));
    }

    @PutMapping("/{id}/cancel-request")
    public ResponseEntity<?> processCancel(
            @PathVariable UUID id,
            @RequestParam boolean approve) {
        adminOrderService.processCancelRequest(id, approve);
        return ResponseEntity.ok(new com.trong.Computer_sell.DTO.response.common.ResponseData<>(200, "Cancel request processed successfully"));
    }
}
