package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.request.Oder.OrderRequest;
import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.oder.OrderResponse;
import com.trong.Computer_sell.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/api/v1/user/orders")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('User','Admin','Staff','SysAdmin')")
public class UserOrderController {

    private final OrderService orderService;

    // Dashboard - Lấy thông tin tổng quan đơn hàng của user
    @GetMapping("/dashboard")
    public ResponseData<Map<String, Object>> getDashboard(@RequestParam UUID userId) {
        try {
            log.info("Get dashboard for user: {}", userId);
            List<OrderResponse> orders = orderService.getOrdersByUser(userId);
            
            if (orders == null) {
                orders = new ArrayList<>();
            }
            
            // Tính toán thống kê
            long totalOrders = orders.size();
            long pendingCount = orders.stream().filter(o -> {
                String status = o.getStatus() != null ? o.getStatus().toString() : "";
                return "PENDING".equals(status);
            }).count();
            long cancelRequestedCount = orders.stream().filter(o -> {
                String status = o.getStatus() != null ? o.getStatus().toString() : "";
                return "CANCEL_REQUEST".equals(status);
            }).count();
            double totalSpent = orders.stream()
                .filter(o -> {
                    String status = o.getStatus() != null ? o.getStatus().toString() : "";
                    return "COMPLETED".equals(status);
                })
                .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0)
                .sum();

            // Lấy 5 đơn hàng gần nhất
            List<Map<String, Object>> recentOrders = orders.stream()
                .sorted((a, b) -> {
                    if (a.getOrderDate() == null) return 1;
                    if (b.getOrderDate() == null) return -1;
                    return b.getOrderDate().compareTo(a.getOrderDate());
                })
                .limit(5)
                .map(o -> {
                    Map<String, Object> map = new HashMap<>();
                    String status = o.getStatus() != null ? o.getStatus().toString() : "";
                    String orderId = o.getId() != null ? o.getId().toString().substring(0, 8).toUpperCase() : "";
                    map.put("id", o.getId());
                    map.put("code", "#" + orderId);
                    map.put("orderDate", o.getOrderDate());
                    map.put("totalAmount", o.getTotalAmount());
                    map.put("status", status);
                    map.put("statusLabel", getStatusLabel(status));
                    map.put("statusTone", getStatusTone(status));
                    map.put("title", "#" + orderId);
                    map.put("subtitle", (o.getDetails() != null ? o.getDetails().size() : 0) + " sản phẩm");
                    map.put("cancellable", "PENDING".equals(status));
                    return map;
                })
                .toList();

            // Build response
            Map<String, Object> result = new HashMap<>();
            
            // Profile info
            Map<String, Object> profile = new HashMap<>();
            profile.put("fullName", "Khách hàng");
            profile.put("avatarText", "KH");
            profile.put("phoneMasked", "***");
            profile.put("badges", List.of("Thành viên"));
            profile.put("membershipMessage", "Cảm ơn bạn đã tin tưởng THComputer");
            profile.put("memberSince", new Date());
            profile.put("nextReviewDate", new Date());
            result.put("profile", profile);

            // Stats
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalOrders", totalOrders);
            stats.put("pendingCount", pendingCount);
            stats.put("cancelRequestedCount", cancelRequestedCount);
            stats.put("totalSpent", totalSpent);
            stats.put("periodLabel", "Tổng chi tiêu");
            result.put("stats", stats);

            // Recent orders
            result.put("recentOrders", recentOrders);
            
            // Offers (empty for now)
            result.put("offers", List.of());

            return new ResponseData<>(200, "Success", result);
        } catch (Exception e) {
            log.error("Error getting dashboard: {}", e.getMessage(), e);
            return new ResponseData<>(500, e.getMessage(), null);
        }
    }

    private String getStatusLabel(String status) {
        return switch (status) {
            case "PENDING" -> "Chờ xác nhận";
            case "CONFIRMED" -> "Đã xác nhận";
            case "PROCESSING" -> "Đang xử lý";
            case "SHIPPING" -> "Đang giao";
            case "COMPLETED" -> "Hoàn thành";
            case "CANCELED" -> "Đã hủy";
            case "CANCEL_REQUEST" -> "Đang chờ hủy";
            default -> status;
        };
    }

    private String getStatusTone(String status) {
        return switch (status) {
            case "PENDING" -> "warning";
            case "CONFIRMED", "PROCESSING" -> "info";
            case "SHIPPING" -> "primary";
            case "COMPLETED" -> "success";
            case "CANCELED" -> "danger";
            case "CANCEL_REQUEST" -> "warning";
            default -> "info";
        };
    }

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
            @RequestParam(required = false, defaultValue = "") String reason) {
        try {
            log.info("Request cancel order: {} with reason: {}", id, reason);
            orderService.requestCancelOrder(id, reason);
            return new ResponseData<>(200, "Cancel request sent successfully", null);
        } catch (Exception e) {
            log.error("Error requesting cancel order: {}", e.getMessage(), e);
            return new ResponseData<>(400, e.getMessage(), null);
        }
    }
}
