package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.Oder.OrderRequest;
import com.trong.Computer_sell.DTO.response.oder.OrderResponse;
import com.trong.Computer_sell.common.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);
    OrderResponse getOrderById(UUID id);
    List<OrderResponse> getOrdersByUser(UUID userId);
    void cancelOrder(UUID id);
    void requestCancelOrder(UUID orderId, String reason);
    OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus);
}
