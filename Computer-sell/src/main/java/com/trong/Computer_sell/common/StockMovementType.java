package com.trong.Computer_sell.common;

/**
 * Loại biến động kho
 * - IMPORT: Nhập kho từ nhà cung cấp
 * - EXPORT: Xuất kho (bán hàng)
 * - RETURN: Hoàn trả kho (hủy đơn)
 * - ADJUSTMENT: Điều chỉnh kho (kiểm kê)
 */
public enum StockMovementType {
    IMPORT,      // Nhập kho
    EXPORT,      // Xuất kho (bán)
    RETURN,      // Hoàn trả (hủy đơn)
    ADJUSTMENT   // Điều chỉnh
}
