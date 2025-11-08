package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.response.oder.OrderResponse;
import com.trong.Computer_sell.common.AddressType;
import com.trong.Computer_sell.common.OrderStatus;
import com.trong.Computer_sell.common.PaymentStatus;
import com.trong.Computer_sell.model.AddressEntity;
import com.trong.Computer_sell.model.OrderEntity;
import com.trong.Computer_sell.model.ShippingOrderEntity;
import com.trong.Computer_sell.repository.OrderRepository;
import com.trong.Computer_sell.repository.ShippingOrderRepository;
import com.trong.Computer_sell.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepository orderRepository;
    private final ShippingOrderRepository shippingOrderRepository;

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
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        OrderStatus currentStatus = order.getStatus();

        // Kiểm tra chuyển trạng thái hợp lệ
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new RuntimeException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        // Cập nhật trạng thái
        order.setStatus(newStatus);
        orderRepository.save(order);
        log.info("Order {} status changed from {} → {}", orderId, currentStatus, newStatus);

        // Nếu chuyển sang SHIPPING => tạo phiếu vận chuyển
        if (newStatus == OrderStatus.SHIPPING) {
            createShippingOrder(order);
        }
    }

    // ================================
    //  4. Duyệt hoặc từ chối yêu cầu hủy đơn
    // ================================
    @Override
    public void processCancelRequest(UUID orderId, boolean approve) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.CANCEL_REQUEST) {
            throw new RuntimeException("Order is not in cancel request state");
        }

        // Duyệt hoặc từ chối yêu cầu hủy
        if (approve) {
            order.setStatus(OrderStatus.CANCELED);
            log.info("🟥 Order {} cancel request APPROVED", orderId);
        } else {
            order.setStatus(OrderStatus.CONFIRMED);
            log.info("🟩 Order {} cancel request REJECTED → set CONFIRMED", orderId);
        }

        orderRepository.save(order);
    }

    // ================================
    // 🔹 5. Hàm hỗ trợ tạo phiếu vận chuyển khi giao hàng
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
            case PENDING -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELED || to == OrderStatus.CANCEL_REQUEST;
            case CANCEL_REQUEST -> to == OrderStatus.CANCELED || to == OrderStatus.CONFIRMED;
            case CONFIRMED -> to == OrderStatus.SHIPPING || to == OrderStatus.CANCELED;
            case SHIPPING -> to == OrderStatus.COMPLETED;
            case COMPLETED, CANCELED -> false;
        };
    }
}
