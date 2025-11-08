package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.request.Oder.OrderRequest;
import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order API", description = "Quản lý đơn hàng và thanh toán")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Tạo đơn hàng mới (tự động áp mã khuyến mãi nếu có)")
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('User','Admin','SysAdmin')")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        try {
            return ResponseEntity.ok(new ResponseData<>(200, "Order created successfully",
                    orderService.createOrder(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseError(400, e.getMessage()));
        }
    }

    @Operation(summary = "Xem chi tiết đơn hàng theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(new ResponseData<>(200, "Success", orderService.getOrderById(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseError(400, e.getMessage()));
        }
    }

    @Operation(summary = "Lấy danh sách đơn hàng của người dùng")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(new ResponseData<>(200, "Success", orderService.getOrdersByUser(userId)));
    }

    @Operation(summary = "Hủy đơn hàng")
    @PutMapping("/cancel/{id}")
    @PreAuthorize("hasAnyAuthority('User','Admin','SysAdmin')")
    public ResponseEntity<?> cancelOrder(@PathVariable UUID id) {
        try {
            orderService.cancelOrder(id);
            return ResponseEntity.ok(new ResponseData<>(200, "Order canceled", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseError(400, e.getMessage()));
        }
    }
}