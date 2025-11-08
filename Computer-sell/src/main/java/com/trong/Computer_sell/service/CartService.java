package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.response.cart.CartResponse;

import java.util.UUID;

public interface CartService {

    CartResponse addToCart(UUID userId, UUID productId, int quantity);

    CartResponse viewCart(UUID userId);

    CartResponse updateQuantity(UUID userId, UUID productId, int newQuantity);

    CartResponse removeItem(UUID userId, UUID productId);
}
