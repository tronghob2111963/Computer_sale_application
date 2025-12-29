package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.response.Shipping.ShippingOrderResponse;
import com.trong.Computer_sell.model.OrderEntity;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

public interface ShippingOrderService {

    List<ShippingOrderResponse> getAllShippingOrders();

    ShippingOrderResponse getShippingOrderById(UUID id);

    ByteArrayInputStream exportShippingOrderToPdf(UUID id);

    ShippingOrderResponse createShippingOrder(OrderEntity order);
}