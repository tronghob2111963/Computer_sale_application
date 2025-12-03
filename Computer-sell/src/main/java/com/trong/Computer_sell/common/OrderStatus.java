package com.trong.Computer_sell.common;

public enum OrderStatus {
    PENDING,        // Đơn mới tạo
    CONFIRMED,
    PROCESSING,
    SHIPPING,       // Đang giao
    COMPLETED,      // Giao thành công
    CANCELED,       // Hủy (do người dùng hoặc quản trị)
    CANCEL_REQUEST  // Người dùng yêu cầu hủy
}
