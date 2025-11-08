package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.response.Shipping.ShippingOrderResponse;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

public interface ShippingOrderService {

    List<ShippingOrderResponse> getAllShippingOrders();

    ShippingOrderResponse getShippingOrderById(UUID id);

    ByteArrayInputStream exportShippingOrderToPdf(UUID id);
}