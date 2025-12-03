package com.trong.Computer_sell.service.impl;


import com.trong.Computer_sell.DTO.request.Oder.OrderDetailRequest;
import com.trong.Computer_sell.DTO.request.Oder.OrderRequest;
import com.trong.Computer_sell.DTO.response.oder.OrderResponse;
import com.trong.Computer_sell.common.OrderStatus;
import com.trong.Computer_sell.common.PaymentMethod;
import com.trong.Computer_sell.common.PaymentStatus;
import com.trong.Computer_sell.common.ProductStatus;
import com.trong.Computer_sell.model.OrderCancelRequestEntity;
import com.trong.Computer_sell.model.*;
import com.trong.Computer_sell.repository.*;
import com.trong.Computer_sell.service.OrderService;
import com.trong.Computer_sell.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j(topic = "ORDER-SERVICE")
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository detailRepository;
    private final OrderPromotionRepository orderPromotionRepository;
    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderCancelRequestRepository cancelRequestRepository;
    private final PaymentRepository paymentRepository;
    private final StockService stockService;

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        var user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Tạo đơn hàng với trạng thái PENDING (CHƯA TRỪ KHO)
        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(String.valueOf(PaymentMethod.valueOf(request.getPaymentMethod())));
        order.setPaymentStatus(PaymentStatus.UNPAID);
        orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;

        // 2. Thêm chi tiết sản phẩm (CHỈ KIỂM TRA TỒN KHO, KHÔNG TRỪ)
        for (OrderDetailRequest item : request.getItems()) {
            ProductEntity product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            // Kiểm tra sản phẩm còn hoạt động không
            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " không còn bán");
            }

            // Kiểm tra tồn kho đủ không (chỉ kiểm tra, chưa trừ)
            int currentStock = product.getStock() != null ? product.getStock() : 0;
            if (currentStock < item.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ tồn kho. Còn: " + currentStock);
            }

            OrderDetailEntity detail = new OrderDetailEntity();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(product.getPrice());
            detail.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            detailRepository.save(detail);
            total = total.add(detail.getSubtotal());

            // KHÔNG TRỪ KHO Ở ĐÂY - Chỉ trừ khi CONFIRMED
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

        // 5. Tự động tạo bản ghi PAYMENT cho đơn hàng
        PaymentEntity payment = PaymentEntity.builder()
                .order(order)
                .amount(total)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.UNPAID)
                .transactionId(null)
                .paymentDate(LocalDateTime.now())
                .note("Payment created automatically when order was created")
                .build();

        paymentRepository.save(payment);

        log.info("Order {} created with status PENDING (stock not deducted yet)", order.getId());
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
        order.setStatus(OrderStatus.CANCELED);
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

        // Lưu yêu cầu hủy hàng
        OrderCancelRequestEntity cancelRequest = OrderCancelRequestEntity.builder()
                .order(order)
                .reason(reason)
                .processed(false)
                .build();

        cancelRequestRepository.save(cancelRequest);
    }

    @Override
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus oldStatus = order.getStatus();

        // 1. Không cho sửa đơn đã hoàn tất hoặc đã hủy
        if (oldStatus == OrderStatus.COMPLETED || oldStatus == OrderStatus.CANCELED) {
            throw new RuntimeException("Cannot modify a completed or canceled order");
        }

        // 2. Kiểm tra trạng thái hợp lệ theo flow
        if (!isValidTransition(oldStatus, newStatus)) {
            throw new RuntimeException("Invalid status transition from " + oldStatus + " to " + newStatus);
        }

        // 3. XỬ LÝ KHO HÀNG THEO TRẠNG THÁI
        handleStockOnStatusChange(order, oldStatus, newStatus);

        // 4. Cập nhật trạng thái đơn
        order.setStatus(newStatus);
        orderRepository.save(order);

        // 5. Nếu COMPLETED → cập nhật payment
        if (newStatus == OrderStatus.COMPLETED) {
            List<PaymentEntity> payments = paymentRepository.findByOrder(order);
            for (PaymentEntity p : payments) {
                if (p.getPaymentStatus() == PaymentStatus.UNPAID) {
                    p.setPaymentStatus(PaymentStatus.SUCCESS);
                    paymentRepository.save(p);
                }
            }
            order.setPaymentStatus(PaymentStatus.PAID);
            orderRepository.save(order);
        }

        log.info("Order {} status changed: {} → {}", orderId, oldStatus, newStatus);
        return OrderResponse.fromEntity(order);
    }

    /**
     * Xử lý kho hàng khi thay đổi trạng thái đơn hàng
     * - PENDING → CONFIRMED: Trừ kho
     * - CONFIRMED/PROCESSING/SHIPPING → CANCELED: Hoàn kho
     */
    private void handleStockOnStatusChange(OrderEntity order, OrderStatus oldStatus, OrderStatus newStatus) {
        String orderId = order.getId().toString();
        String username = order.getUser().getUsername();

        // Trừ kho khi xác nhận đơn hàng
        if (oldStatus == OrderStatus.PENDING && newStatus == OrderStatus.CONFIRMED) {
            log.info("Deducting stock for order {}", orderId);
            for (OrderDetailEntity detail : order.getOrderDetails()) {
                stockService.exportStock(
                        detail.getProduct().getId(),
                        detail.getQuantity(),
                        detail.getUnitPrice(),
                        orderId,
                        "ORDER",
                        "Xuất kho cho đơn hàng " + orderId,
                        username
                );
            }
        }

        // Hoàn kho khi hủy đơn (chỉ hoàn nếu đã trừ kho - tức là đã CONFIRMED trở lên)
        if (newStatus == OrderStatus.CANCELED && isStockDeducted(oldStatus)) {
            log.info("Returning stock for canceled order {}", orderId);
            for (OrderDetailEntity detail : order.getOrderDetails()) {
                stockService.returnStock(
                        detail.getProduct().getId(),
                        detail.getQuantity(),
                        detail.getUnitPrice(),
                        orderId,
                        "ORDER_CANCEL",
                        "Hoàn kho do hủy đơn hàng " + orderId,
                        username
                );
            }
        }
    }

    /**
     * Kiểm tra đơn hàng đã trừ kho chưa (đã qua trạng thái CONFIRMED)
     */
    private boolean isStockDeducted(OrderStatus status) {
        return status == OrderStatus.CONFIRMED ||
               status == OrderStatus.PROCESSING ||
               status == OrderStatus.SHIPPING ||
               status == OrderStatus.CANCEL_REQUEST;
    }

    private boolean isValidTransition(OrderStatus oldStatus, OrderStatus newStatus) {
        return switch (oldStatus) {
            case PENDING -> List.of(OrderStatus.CONFIRMED, OrderStatus.CANCELED, OrderStatus.CANCEL_REQUEST).contains(newStatus);
            case CONFIRMED -> List.of(OrderStatus.PROCESSING, OrderStatus.SHIPPING, OrderStatus.CANCELED, OrderStatus.CANCEL_REQUEST).contains(newStatus);
            case PROCESSING -> List.of(OrderStatus.SHIPPING, OrderStatus.CANCELED).contains(newStatus);
            case SHIPPING -> List.of(OrderStatus.COMPLETED, OrderStatus.CANCEL_REQUEST).contains(newStatus);
            case CANCEL_REQUEST -> List.of(OrderStatus.CANCELED, OrderStatus.CONFIRMED).contains(newStatus);
            default -> false;
        };
    }
}

