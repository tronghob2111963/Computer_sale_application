-- =====================================================
-- MIGRATION: Hệ thống quản lý kho hàng chuẩn E-commerce
-- CHẠY FILE NÀY TRƯỚC KHI KHỞI ĐỘNG ỨNG DỤNG
-- =====================================================

-- 1. Thêm cột status cho bảng tbl_products (soft delete)
-- Bước 1: Thêm cột cho phép NULL trước
ALTER TABLE tbl_products ADD COLUMN IF NOT EXISTS status VARCHAR(20);

-- Bước 2: Cập nhật tất cả sản phẩm hiện có thành ACTIVE
UPDATE tbl_products SET status = 'ACTIVE' WHERE status IS NULL;

-- Bước 3: (Tùy chọn) Thêm constraint NOT NULL sau khi đã có dữ liệu
-- ALTER TABLE tbl_products ALTER COLUMN status SET NOT NULL;

-- 2. Tạo bảng lịch sử kho hàng
CREATE TABLE IF NOT EXISTS tbl_stock_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES tbl_products(id),
    movement_type VARCHAR(20) NOT NULL, -- IMPORT, EXPORT, RETURN, ADJUSTMENT
    quantity INTEGER NOT NULL,
    stock_before INTEGER NOT NULL,
    stock_after INTEGER NOT NULL,
    unit_price DECIMAL(12,2),
    reference_id VARCHAR(100), -- Mã phiếu nhập hoặc mã đơn hàng
    reference_type VARCHAR(50), -- IMPORT_RECEIPT, ORDER, ORDER_CANCEL, ADJUSTMENT
    note TEXT,
    created_by VARCHAR(100),
    movement_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index cho tìm kiếm nhanh
CREATE INDEX IF NOT EXISTS idx_stock_history_product ON tbl_stock_history(product_id);
CREATE INDEX IF NOT EXISTS idx_stock_history_movement_type ON tbl_stock_history(movement_type);
CREATE INDEX IF NOT EXISTS idx_stock_history_movement_date ON tbl_stock_history(movement_date);
CREATE INDEX IF NOT EXISTS idx_stock_history_reference ON tbl_stock_history(reference_id, reference_type);

-- 3. Tạo bảng phiếu nhập kho (nếu chưa có)
CREATE TABLE IF NOT EXISTS tbl_import_receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES tbl_employees(id),
    receipt_code VARCHAR(50) UNIQUE NOT NULL,
    receipt_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12,2) DEFAULT 0,
    note TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, COMPLETED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Tạo bảng chi tiết phiếu nhập kho (nếu chưa có)
CREATE TABLE IF NOT EXISTS tbl_import_receipt_details (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    receipt_id UUID NOT NULL REFERENCES tbl_import_receipts(id),
    product_id UUID NOT NULL REFERENCES tbl_products(id),
    quantity INTEGER NOT NULL,
    import_price DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index cho chi tiết phiếu nhập
CREATE INDEX IF NOT EXISTS idx_import_receipt_details_receipt ON tbl_import_receipt_details(receipt_id);
CREATE INDEX IF NOT EXISTS idx_import_receipt_details_product ON tbl_import_receipt_details(product_id);

-- =====================================================
-- COMMENT: Giải thích các trạng thái
-- =====================================================
-- ProductStatus:
--   ACTIVE: Sản phẩm đang bán
--   INACTIVE: Tạm ngưng bán
--   DELETED: Đã xóa (soft delete)

-- StockMovementType:
--   IMPORT: Nhập kho từ nhà cung cấp
--   EXPORT: Xuất kho (bán hàng - khi đơn CONFIRMED)
--   RETURN: Hoàn trả kho (khi hủy đơn)
--   ADJUSTMENT: Điều chỉnh kho (kiểm kê)

-- ImportReceiptStatus:
--   PENDING: Đang chờ xử lý
--   COMPLETED: Đã hoàn thành
--   CANCELLED: Đã hủy
