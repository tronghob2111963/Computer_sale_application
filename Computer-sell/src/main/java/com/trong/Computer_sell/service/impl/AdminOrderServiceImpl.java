package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.response.oder.OrderResponse;
import com.trong.Computer_sell.common.AddressType;
import com.trong.Computer_sell.common.OrderStatus;
import com.trong.Computer_sell.common.PaymentStatus;
import com.trong.Computer_sell.model.AddressEntity;
import com.trong.Computer_sell.model.OrderDetailEntity;
import com.trong.Computer_sell.model.OrderEntity;
import com.trong.Computer_sell.model.ShippingOrderEntity;
import com.trong.Computer_sell.repository.OrderRepository;
import com.trong.Computer_sell.repository.ShippingOrderRepository;
import com.trong.Computer_sell.service.AdminOrderService;
import com.trong.Computer_sell.service.NotificationService;
import com.trong.Computer_sell.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepository orderRepository;
    private final ShippingOrderRepository shippingOrderRepository;
    private final StockService stockService;
    private final NotificationService notificationService;

    // ================================
    // 1. Lấy toàn bộ đơn hàng
    // ================================
    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    // ================================
    // 2. Lọc đơn hàng theo trạng thái hoặc thời gian
    // ================================
    @Override
    public List<OrderResponse> filterOrders(OrderStatus status, LocalDateTime start, LocalDateTime end) {
        List<OrderEntity> orders;

        if (status != null) {
            orders = orderRepository.findByStatus(status);
        } else if (start != null && end != null) {
            orders = orderRepository.findByDateRange(start, end);
        } else {
            orders = orderRepository.findAll();
        }

        return orders.stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    // ================================
    // 3. Cập nhật trạng thái đơn hàng (có kiểm tra logic)
    // ================================
    @Override
    public void updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        // Fetch order cùng với user để tránh lazy loading issues khi gửi thông báo
        OrderEntity order = orderRepository.findByIdWithUser(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        OrderStatus currentStatus = order.getStatus();

        // Kiểm tra chuyển trạng thái hợp lệ
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new RuntimeException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        // XỬ LÝ KHO HÀNG
        handleStockOnStatusChange(order, currentStatus, newStatus);

        // Cập nhật trạng thái
        order.setStatus(newStatus);
        orderRepository.save(order);
        log.info("Order {} status changed from {} → {}", orderId, currentStatus, newStatus);

        // Gửi thông báo cho user về thay đổi trạng thái đơn hàng
        try {
            if (order.getUser() != null) {
                notificationService.notifyOrderStatusChanged(
                    order.getUser().getId(),
                    orderId,
                    currentStatus.name(),
                    newStatus.name()
                );
                log.info("Sent order status notification to user {} for order {}", order.getUser().getId(), orderId);
            } else {
                log.warn("Order {} has no user, skipping notification", orderId);
            }
        } catch (Exception e) {
            log.error("Failed to send notification for order {}: {}", orderId, e.getMessage());
        }

        // Nếu chuyển sang SHIPPING => tạo phiếu vận chuyển
        if (newStatus == OrderStatus.SHIPPING) {
            createShippingOrder(order);
        }
    }

    /**
     * Xử lý kho hàng khi thay đổi trạng thái đơn hàng
     */
    private void handleStockOnStatusChange(OrderEntity order, OrderStatus oldStatus, OrderStatus newStatus) {
        String orderId = order.getId().toString();
        String adminUser = "ADMIN"; // Có thể lấy từ SecurityContext

        // Trừ kho khi xác nhận đơn hàng (PENDING → CONFIRMED)
        if (oldStatus == OrderStatus.PENDING && newStatus == OrderStatus.CONFIRMED) {
            log.info("Admin confirming order {} - Deducting stock", orderId);
            for (OrderDetailEntity detail : order.getOrderDetails()) {
                stockService.exportStock(
                        detail.getProduct().getId(),
                        detail.getQuantity(),
                        detail.getUnitPrice(),
                        orderId,
                        "ORDER",
                        "Xuất kho cho đơn hàng " + orderId + " (Admin xác nhận)",
                        adminUser
                );
            }
        }

        // Hoàn kho khi hủy đơn (chỉ hoàn nếu đã trừ kho)
        if (newStatus == OrderStatus.CANCELED && isStockDeducted(oldStatus)) {
            log.info("Admin canceling order {} - Returning stock", orderId);
            for (OrderDetailEntity detail : order.getOrderDetails()) {
                stockService.returnStock(
                        detail.getProduct().getId(),
                        detail.getQuantity(),
                        detail.getUnitPrice(),
                        orderId,
                        "ORDER_CANCEL",
                        "Hoàn kho do Admin hủy đơn hàng " + orderId,
                        adminUser
                );
            }
        }
    }

    /**
     * Kiểm tra đơn hàng đã trừ kho chưa
     */
    private boolean isStockDeducted(OrderStatus status) {
        return status == OrderStatus.CONFIRMED ||
               status == OrderStatus.PROCESSING ||
               status == OrderStatus.SHIPPING ||
               status == OrderStatus.CANCEL_REQUEST;
    }

    // ================================
    //  4. Duyệt hoặc từ chối yêu cầu hủy đơn
    // ================================
    @Override
    public void processCancelRequest(UUID orderId, boolean approve) {
        // Fetch order cùng với user để tránh lazy loading issues khi gửi thông báo
        OrderEntity order = orderRepository.findByIdWithUser(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.CANCEL_REQUEST) {
            throw new RuntimeException("Order is not in cancel request state");
        }

        String adminUser = "ADMIN";

        // Duyệt hoặc từ chối yêu cầu hủy
        if (approve) {
            // Hoàn kho khi duyệt hủy đơn (vì đơn đã CONFIRMED nên đã trừ kho)
            log.info("Approving cancel request for order {} - Returning stock", orderId);
            for (OrderDetailEntity detail : order.getOrderDetails()) {
                stockService.returnStock(
                        detail.getProduct().getId(),
                        detail.getQuantity(),
                        detail.getUnitPrice(),
                        orderId.toString(),
                        "ORDER_CANCEL",
                        "Hoàn kho do duyệt yêu cầu hủy đơn " + orderId,
                        adminUser
                );
            }
            order.setStatus(OrderStatus.CANCELED);
            log.info("Order {} cancel request APPROVED - Stock returned", orderId);
            
            // Gửi thông báo cho user về việc yêu cầu hủy được duyệt
            try {
                if (order.getUser() != null) {
                    notificationService.notifyOrderStatusChanged(
                        order.getUser().getId(),
                        orderId,
                        OrderStatus.CANCEL_REQUEST.name(),
                        OrderStatus.CANCELED.name()
                    );
                    log.info("Sent cancel approved notification to user {}", order.getUser().getId());
                }
            } catch (Exception e) {
                log.error("Failed to send cancel approved notification for order {}: {}", orderId, e.getMessage());
            }
        } else {
            order.setStatus(OrderStatus.CONFIRMED);
            log.info("Order {} cancel request REJECTED → set CONFIRMED", orderId);
            
            // Gửi thông báo cho user về việc yêu cầu hủy bị từ chối
            try {
                if (order.getUser() != null) {
                    notificationService.notifyOrderStatusChanged(
                        order.getUser().getId(),
                        orderId,
                        OrderStatus.CANCEL_REQUEST.name(),
                        OrderStatus.CONFIRMED.name()
                    );
                    log.info("Sent cancel rejected notification to user {}", order.getUser().getId());
                }
            } catch (Exception e) {
                log.error("Failed to send cancel rejected notification for order {}: {}", orderId, e.getMessage());
            }
        }

        orderRepository.save(order);
    }

    // ================================
    //  5. Hàm hỗ trợ tạo phiếu vận chuyển khi giao hàng
    // ================================
    private void createShippingOrder(OrderEntity order) {
        var user = order.getUser();

        // Tìm địa chỉ giao hàng chính (AddressType.SHIPPING hoặc mặc định)
        AddressEntity address = user.getAddresses().stream()
                .filter(a -> a.getAddressType() == AddressType.HOME)
                .findFirst()
                .orElseGet(() -> user.getAddresses().stream().findFirst().orElse(null));

        if (address == null) {
            throw new RuntimeException("User does not have a valid shipping address");
        }

        // Ghép địa chỉ đầy đủ
        String fullAddress = String.join(", ",
                address.getApartmentNumber() != null ? address.getApartmentNumber() : "",
                address.getStreetNumber() != null ? address.getStreetNumber() : "",
                address.getWard() != null ? address.getWard() : "",
                address.getCity() != null ? address.getCity() : ""
        ).replaceAll(",\\s*,", ",").trim();

        // Tạo phiếu vận chuyển
        ShippingOrderEntity shipping = ShippingOrderEntity.builder()
                .order(order)
                .recipientName(user.getFirstName() + " " + user.getLastName())
                .recipientPhone(user.getPhone())
                .shippingAddress(fullAddress)
                .paymentCompleted(order.getPaymentStatus() == PaymentStatus.PAID)
                .totalAmount(order.getTotalAmount())
                .createdAt(LocalDateTime.now())
                .build();

        shippingOrderRepository.save(shipping);
        log.info("Shipping order created for Order ID {}", order.getId());
    }

    // ================================
    //  6. Kiểm tra chuyển trạng thái hợp lệ
    // ================================
    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        return switch (from) {

            case PENDING ->
                    to == OrderStatus.CONFIRMED
                            || to == OrderStatus.CANCELED
                            || to == OrderStatus.CANCEL_REQUEST;

            case CONFIRMED ->
                    to == OrderStatus.PROCESSING
                            || to == OrderStatus.CANCELED;

            case PROCESSING ->
                    to == OrderStatus.SHIPPING
                            || to == OrderStatus.CANCELED;

            case SHIPPING ->
                    to == OrderStatus.COMPLETED;

            case CANCEL_REQUEST ->
                    to == OrderStatus.CANCELED
                            || to == OrderStatus.CONFIRMED;

            case COMPLETED, CANCELED ->
                    false;
        };
    }

}
