package com.trong.Computer_sell.DTO.response.oder;

import com.trong.Computer_sell.DTO.response.payment.PaymentResponse;
import com.trong.Computer_sell.model.AddressEntity;
import com.trong.Computer_sell.model.OrderEntity;
import com.trong.Computer_sell.model.OrderPromotionEntity;
import com.trong.Computer_sell.model.PromotionEntity;
import com.trong.Computer_sell.common.PaymentMethod;
import com.trong.Computer_sell.common.PaymentStatus;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

    private UUID id;
    private String status;
    private BigDecimal totalAmount;

    private String User_fullName;
    private String User_phone;

    private BigDecimal discount;

    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    private LocalDateTime orderDate;

    private List<OrderDetailResponse> details;

    //  Thêm danh sách thanh toán của đơn hàng
    private List<PaymentResponse> payments;

    //  (optional) Nếu muốn trả mã giảm
    private String promoCode;

    // Shipping address
    private ShippingAddressDTO shippingAddress;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShippingAddressDTO {
        private UUID id;
        private String apartmentNumber;
        private String streetNumber;
        private String ward;
        private String city;
        private String addressType;
        private String fullAddress;
    }

    public static OrderResponse fromEntity(OrderEntity entity) {

        // Lấy mã giảm giá (nếu có)
        String promoCode = entity.getOrderPromotions().isEmpty()
                ? null
                : entity.getOrderPromotions().stream()
                .map(OrderPromotionEntity::getPromotion)
                .map(PromotionEntity::getPromoCode)
                .findFirst()
                .orElse(null);

        // Lấy discount (nếu có)
        BigDecimal discount = entity.getOrderPromotions().isEmpty()
                ? BigDecimal.ZERO
                : entity.getOrderPromotions().stream()
                .map(OrderPromotionEntity::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build shipping address DTO
        ShippingAddressDTO addressDTO = null;
        if (entity.getShippingAddress() != null) {
            AddressEntity addr = entity.getShippingAddress();
            String fullAddress = String.format("%s, %s, %s, %s",
                    addr.getApartmentNumber() != null ? addr.getApartmentNumber() : "",
                    addr.getStreetNumber() != null ? addr.getStreetNumber() : "",
                    addr.getWard() != null ? addr.getWard() : "",
                    addr.getCity() != null ? addr.getCity() : "");
            addressDTO = ShippingAddressDTO.builder()
                    .id(addr.getId())
                    .apartmentNumber(addr.getApartmentNumber())
                    .streetNumber(addr.getStreetNumber())
                    .ward(addr.getWard())
                    .city(addr.getCity())
                    .addressType(addr.getAddressType() != null ? addr.getAddressType().name() : "HOME")
                    .fullAddress(fullAddress)
                    .build();
        }

        return OrderResponse.builder()
                .id(entity.getId())
                .status(entity.getStatus().name())
                .totalAmount(entity.getTotalAmount())
                .orderDate(entity.getOrderDate())

                // User info
                .User_fullName(entity.getUser().getFirstName() + " " + entity.getUser().getLastName())
                .User_phone(entity.getUser().getPhone())

                // Thanh toán
                .paymentMethod(PaymentMethod.valueOf(entity.getPaymentMethod()))
                .paymentStatus(entity.getPaymentStatus())

                // Chi tiết đơn
                .details(entity.getOrderDetails().stream()
                        .map(OrderDetailResponse::fromEntity)
                        .collect(Collectors.toList()))

                // 🔥 TRẢ VỀ DANH SÁCH PAYMENT
                .payments(entity.getPayments().stream()
                        .map(PaymentResponse::fromEntity)
                        .collect(Collectors.toList()))

                // Giảm giá
                .discount(discount)

                // Mã khuyến mãi
                .promoCode(promoCode)

                // Địa chỉ giao hàng
                .shippingAddress(addressDTO)

                .build();
    }
}
