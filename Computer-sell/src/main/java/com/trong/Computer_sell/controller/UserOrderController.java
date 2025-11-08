package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.request.Oder.OrderRequest;
import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.oder.OrderResponse;
import com.trong.Computer_sell.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/user/orders")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('User','Admin','Staff','SysAdmin')")
public class UserOrderController {

    private final OrderService orderService;

    // Tạo đơn hàng mới
    @PostMapping
    public ResponseData<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        try {
            log.info("Create order request: {}", request);
            return new ResponseData<>(200, "Order created successfully",
                    orderService.createOrder(request));
        } catch (Exception e) {
            return new ResponseData<>(400, e.getMessage(), null);
        }
    }

    // Lấy danh sách đơn hàng của user
    @GetMapping
    public ResponseData<List<OrderResponse>> getMyOrders(@RequestParam UUID userId) {
        try {
            log.info("Get my orders request: {}", userId);
            return new ResponseData<>(200, "Success", orderService.getOrdersByUser(userId));
        } catch (Exception e) {
            return new ResponseData<>(400, e.getMessage(), null);
        }
    }

    // Xem chi tiết đơn hàng
    @GetMapping("/{id}")
    public ResponseData<OrderResponse> getOrderDetail(@PathVariable UUID id) {
        try {
            log.info("Get order detail request: {}", id);
            return new ResponseData<>(200, "Success", orderService.getOrderById(id));
        } catch (Exception e) {
            return new ResponseData<>(400, e.getMessage(), null);
        }
    }

    // Gửi yêu cầu hủy đơn hàng
    @PutMapping("/{id}/cancel-request")
    public ResponseData<String> requestCancelOrder(
            @PathVariable UUID id,
            @RequestParam String reason) {
        try {
            log.info("Request cancel order request: {}", id);
            orderService.requestCancelOrder(id, reason);
            return new ResponseData<>(200, "Cancel request sent successfully with reason: " + reason, null);
        } catch (Exception e) {
            return new ResponseData<>(400, e.getMessage(), null);
        }
    }
}
