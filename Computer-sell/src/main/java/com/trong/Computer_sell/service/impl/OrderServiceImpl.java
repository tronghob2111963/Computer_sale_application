package com.trong.Computer_sell.service.impl;


import com.trong.Computer_sell.DTO.request.Oder.OrderDetailRequest;
import com.trong.Computer_sell.DTO.request.Oder.OrderRequest;
import com.trong.Computer_sell.DTO.response.oder.OrderResponse;
import com.trong.Computer_sell.common.OrderStatus;
import com.trong.Computer_sell.common.PaymentMethod;
import com.trong.Computer_sell.common.PaymentStatus;
import com.trong.Computer_sell.controller.OrderCancelRequestEntity;
import com.trong.Computer_sell.model.OrderDetailEntity;
import com.trong.Computer_sell.model.OrderEntity;
import com.trong.Computer_sell.model.OrderPromotionEntity;
import com.trong.Computer_sell.model.ProductEntity;
import com.trong.Computer_sell.repository.*;
import com.trong.Computer_sell.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository detailRepository;
    private final OrderPromotionRepository orderPromotionRepository;
    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderCancelRequestRepository cancelRequestRepository;

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        var user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  1. Tạo đơn hàng
        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.valueOf("PENDING"));
        order.setPaymentMethod(String.valueOf(PaymentMethod.valueOf(request.getPaymentMethod())));
        order.setPaymentStatus(PaymentStatus.valueOf("UNPAID"));
        orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;

        //  2. Thêm chi tiết sản phẩm
        for (OrderDetailRequest item : request.getItems()) {
            ProductEntity product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            OrderDetailEntity detail = new OrderDetailEntity();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(product.getPrice());
            detail.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            detailRepository.save(detail);

            total = total.add(detail.getSubtotal());

            // Cập nhật tồn kho
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }

        // 3. Áp mã khuyến mãi (nếu có)
        if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
            var promo = promotionRepository.findValidPromotionByCode(request.getPromoCode(), LocalDateTime.now())
                    .orElseThrow(() -> new RuntimeException("Invalid or expired promotion"));

            BigDecimal discount = total.multiply(promo.getDiscountPercent()).divide(BigDecimal.valueOf(100));
            total = total.subtract(discount);

            OrderPromotionEntity orderPromo = new OrderPromotionEntity();
            orderPromo.setOrder(order);
            orderPromo.setPromotion(promo);
            orderPromo.setDiscountAmount(discount);
            orderPromotionRepository.save(orderPromo);
        }

        // 4. Cập nhật tổng tiền
        order.setTotalAmount(total);
        orderRepository.saveAndFlush(order);

        return OrderResponse.fromEntity(order);
    }

    @Override
    public OrderResponse getOrderById(UUID id) {
        OrderEntity order = orderRepository.findWithPromotionsAndDetailsById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return OrderResponse.fromEntity(order);
    }

    @Override
    public List<OrderResponse> getOrdersByUser(UUID userId) {
        return orderRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    @Override
    public void cancelOrder(UUID id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(OrderStatus.valueOf("CANCELED"));
        orderRepository.save(order);
    }

    @Override
    public void requestCancelOrder(UUID orderId, String reason) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("You can only request cancel when order is still PENDING");
        }

        order.setStatus(OrderStatus.CANCEL_REQUEST);
        orderRepository.save(order);

        // Lưu yêu cầu hủy hàng kèm lý do
        OrderCancelRequestEntity cancelRequest = OrderCancelRequestEntity.builder()
                .order(order)
                .reason(reason)
                .processed(false)
                .build();

        cancelRequestRepository.save(cancelRequest);
    }


}