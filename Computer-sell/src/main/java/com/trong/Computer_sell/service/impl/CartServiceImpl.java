package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.response.cart.CartItemResponse;
import com.trong.Computer_sell.DTO.response.cart.CartResponse;
import com.trong.Computer_sell.model.CartEntity;
import com.trong.Computer_sell.model.CartItemEntity;
import com.trong.Computer_sell.model.ProductEntity;
import com.trong.Computer_sell.repository.CartItemRepository;
import com.trong.Computer_sell.repository.CartRepository;
import com.trong.Computer_sell.repository.ProductRepository;
import com.trong.Computer_sell.repository.UserRepository;
import com.trong.Computer_sell.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.trong.Computer_sell.DTO.response.cart.CartResponse.*;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // Tạo hoặc lấy giỏ hàng hiện tại (ACTIVE)
    private CartEntity getOrCreateActiveCart(UUID userId) {
        return cartRepository.findActiveCartByUserId(userId)
                .orElseGet(() -> {
                    CartEntity newCart = new CartEntity();
                    newCart.setUser(userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found")));
                    newCart.setStatus("ACTIVE");
                    newCart.setTotalPrice(BigDecimal.ZERO);
                    return cartRepository.save(newCart);
                });
    }

    // Thêm sản phẩm vào giỏ hàng
    @Override
    public CartResponse addToCart(UUID userId, UUID productId, int quantity) {
        CartEntity cart = getOrCreateActiveCart(userId);
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItemEntity> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        // Nếu sản phẩm đã tồn tại trong giỏ hàng, cập nhật số lượng
        if (existingItem.isPresent()) {
            CartItemEntity item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            cartItemRepository.save(item);
        } else { // Nếu sản phẩm chưa tồn tại trong giỏ hàng, thêm vào giỏ hàng
            CartItemEntity newItem = new CartItemEntity();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setUnitPrice(product.getPrice());
            newItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            cartItemRepository.save(newItem);
        }

        updateCartTotal(cart.getId());
        return toResponse(cartRepository.findById(cart.getId()).get());
    }

    // Xem giỏ hàng của user
    @Override
    public CartResponse viewCart(UUID userId) {
        CartEntity cart = getOrCreateActiveCart(userId);
        return toResponse(cart);
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    @Override
    public CartResponse updateQuantity(UUID userId, UUID productId, int newQuantity) {
        CartEntity cart = getOrCreateActiveCart(userId);
        CartItemEntity item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        // Nếu số lượng mới <= 0, xóa sản phẩm khỏi giỏ hàng
        if (newQuantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(newQuantity);
            item.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity)));
            cartItemRepository.save(item);
        }

        updateCartTotal(cart.getId());
        return toResponse(cartRepository.findById(cart.getId()).get());
    }

    //  Xóa sản phẩm khỏi giỏ hàng
    @Override
    public CartResponse removeItem(UUID userId, UUID productId) {
        CartEntity cart = getOrCreateActiveCart(userId);
        cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .ifPresent(cartItemRepository::delete);

        updateCartTotal(cart.getId());
        return toResponse(cartRepository.findById(cart.getId()).get());
    }

    // Cập nhật tổng tiền giỏ hàng
    private void updateCartTotal(UUID cartId) {
        BigDecimal total = cartItemRepository.findByCartId(cartId).stream()
                .map(CartItemEntity::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartEntity cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.setTotalPrice(total);
        cartRepository.save(cart);
    }

    // Convert Entity → DTO
    private CartResponse toResponse(CartEntity cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(i -> CartItemResponse.builder()
                        .productId(i.getProduct().getId())
                        .productName(i.getProduct().getName())
                        .price(i.getUnitPrice())
                        .quantity(i.getQuantity())
                        .subtotal(i.getSubtotal())
                        .productImg(i.getProduct().getImages().stream().findFirst().get().getImageUrl())
                        .build()
                ).collect(Collectors.toList());

       return builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getId())
                .totalPrice(cart.getTotalPrice())
                .status(cart.getStatus())
                .items(items)
                .build();
    }
}
