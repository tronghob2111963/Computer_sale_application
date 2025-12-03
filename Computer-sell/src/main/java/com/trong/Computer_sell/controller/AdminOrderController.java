package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.DTO.response.oder.OrderResponse;
import com.trong.Computer_sell.common.OrderStatus;
import com.trong.Computer_sell.service.AdminOrderService;
import com.trong.Computer_sell.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final OrderService orderService;

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



    @PutMapping("/{id}/cancel-request")
    public ResponseEntity<?> processCancel(
            @PathVariable UUID id,
            @RequestParam boolean approve) {
        adminOrderService.processCancelRequest(id, approve);
        return ResponseEntity.ok(new com.trong.Computer_sell.DTO.response.common.ResponseData<>(200, "Cancel request processed successfully"));
    }

    @PutMapping("/{Id}/status")
    @PreAuthorize("hasAnyAuthority('Admin','SysAdmin')")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable UUID Id,
            @RequestParam OrderStatus status
    ) {
        try {
            return ResponseEntity.ok(new ResponseData<>(200, "Order status updated",
                    orderService.updateOrderStatus(Id, status)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseError(400, e.getMessage()));
        }
    }
}
