package com.trong.Computer_sell.common;

/**
 * Trạng thái sản phẩm
 * - ACTIVE: Đang bán
 * - INACTIVE: Tạm ngưng bán
 * - DELETED: Đã xóa (soft delete)
 */
public enum ProductStatus {
    ACTIVE,
    INACTIVE,
    DELETED
}
