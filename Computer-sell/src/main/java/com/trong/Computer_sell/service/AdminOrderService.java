package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.response.oder.OrderResponse;
import com.trong.Computer_sell.common.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AdminOrderService {
    List<OrderResponse> getAllOrders();

    List<OrderResponse> filterOrders(OrderStatus status, LocalDateTime start, LocalDateTime end);

    void updateOrderStatus(UUID orderId, OrderStatus newStatus);

    void processCancelRequest(UUID orderId, boolean approve);
}
