-- =====================================================
-- DATABASE SEEDING: PRODUCTS CHO CỬA HÀNG MÁY TÍNH
-- =====================================================
-- Dựa trên dữ liệu categories, brands, product_types đã có

-- Lưu ý: Sử dụng UUID từ các bảng đã có
-- Categories: PC, Laptop, Màn hình, Phụ kiện, Gaming Gear, Lưu trữ, Thiết bị mạng, Linh kiện máy tính
-- Product Types: CPU, MAINBOARD, RAM, GPU, PSU, CASE, STORAGE, COOLER, SOUND_CARD, NETWORK_CARD, Màn Hình, Gaming Gear
-- Brands: ASUS, ACER, MSI, GIGABYTE, Dell, HP, Lenovo, Samsung, Kingston, Corsair, Logitech, Razer, Intel, AMD, etc.

-- =====================================================
-- 1. CPU - Bộ vi xử lý
-- =====================================================
INSERT INTO tbl_products (id, name, description, price, stock, category_id, brand_id, product_type_id, warranty_period, status, created_at, updated_at)
VALUES
-- Intel CPUs
(gen_random_uuid(), 'Intel Core i9-14900K', 'Bộ vi xử lý Intel Core i9 thế hệ 14, 24 nhân 32 luồng, xung nhịp tối đa 6.0GHz, socket LGA1700, TDP 125W', 14990000, 25, 
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Intel'),
 (SELECT id FROM tbl_product_types WHERE name = 'CPU'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Intel Core i7-14700K', 'Bộ vi xử lý Intel Core i7 thế hệ 14, 20 nhân 28 luồng, xung nhịp tối đa 5.6GHz, socket LGA1700, TDP 125W', 9990000, 40,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Intel'),
 (SELECT id FROM tbl_product_types WHERE name = 'CPU'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Intel Core i5-14600K', 'Bộ vi xử lý Intel Core i5 thế hệ 14, 14 nhân 20 luồng, xung nhịp tối đa 5.3GHz, socket LGA1700, TDP 125W', 7490000, 50,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Intel'),
 (SELECT id FROM tbl_product_types WHERE name = 'CPU'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Intel Core i5-13400F', 'Bộ vi xử lý Intel Core i5 thế hệ 13, 10 nhân 16 luồng, không GPU tích hợp, socket LGA1700', 4590000, 60,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Intel'),
 (SELECT id FROM tbl_product_types WHERE name = 'CPU'),
 36, 'ACTIVE', NOW(), NOW()),

-- AMD CPUs
(gen_random_uuid(), 'AMD Ryzen 9 7950X', 'Bộ vi xử lý AMD Ryzen 9 7950X, 16 nhân 32 luồng, xung nhịp tối đa 5.7GHz, socket AM5, TDP 170W', 13990000, 20,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'AMD'),
 (SELECT id FROM tbl_product_types WHERE name = 'CPU'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'AMD Ryzen 7 7800X3D', 'Bộ vi xử lý AMD Ryzen 7 7800X3D, 8 nhân 16 luồng, 96MB 3D V-Cache, socket AM5, gaming CPU tốt nhất', 10990000, 30,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'AMD'),
 (SELECT id FROM tbl_product_types WHERE name = 'CPU'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'AMD Ryzen 5 7600X', 'Bộ vi xử lý AMD Ryzen 5 7600X, 6 nhân 12 luồng, xung nhịp tối đa 5.3GHz, socket AM5', 5990000, 45,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'AMD'),
 (SELECT id FROM tbl_product_types WHERE name = 'CPU'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'AMD Ryzen 5 5600', 'Bộ vi xử lý AMD Ryzen 5 5600, 6 nhân 12 luồng, socket AM4, giá tốt cho gaming', 3290000, 70,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'AMD'),
 (SELECT id FROM tbl_product_types WHERE name = 'CPU'),
 36, 'ACTIVE', NOW(), NOW());


-- =====================================================
-- 2. MAINBOARD - Bo mạch chủ
-- =====================================================
INSERT INTO tbl_products (id, name, description, price, stock, category_id, brand_id, product_type_id, warranty_period, status, created_at, updated_at)
VALUES
-- ASUS Mainboards
(gen_random_uuid(), 'ASUS ROG STRIX Z790-E Gaming WiFi', 'Bo mạch chủ ASUS ROG STRIX Z790-E, socket LGA1700, DDR5, PCIe 5.0, WiFi 6E, 2.5G LAN', 9990000, 20,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'ASUS'),
 (SELECT id FROM tbl_product_types WHERE name = 'MAINBOARD'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'ASUS TUF Gaming B760M-Plus WiFi D4', 'Bo mạch chủ ASUS TUF Gaming B760M, socket LGA1700, DDR4, mATX, WiFi 6', 4290000, 35,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'ASUS'),
 (SELECT id FROM tbl_product_types WHERE name = 'MAINBOARD'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'ASUS Prime B650M-A WiFi II', 'Bo mạch chủ ASUS Prime B650M-A, socket AM5, DDR5, mATX, WiFi 6, hỗ trợ Ryzen 7000', 4590000, 30,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'ASUS'),
 (SELECT id FROM tbl_product_types WHERE name = 'MAINBOARD'),
 36, 'ACTIVE', NOW(), NOW()),

-- MSI Mainboards
(gen_random_uuid(), 'MSI MAG Z790 Tomahawk WiFi', 'Bo mạch chủ MSI MAG Z790 Tomahawk, socket LGA1700, DDR5, ATX, WiFi 6E, 2.5G LAN', 7990000, 25,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'MSI'),
 (SELECT id FROM tbl_product_types WHERE name = 'MAINBOARD'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'MSI PRO B760M-A WiFi DDR4', 'Bo mạch chủ MSI PRO B760M-A, socket LGA1700, DDR4, mATX, WiFi 6, giá tốt', 3290000, 45,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'MSI'),
 (SELECT id FROM tbl_product_types WHERE name = 'MAINBOARD'),
 36, 'ACTIVE', NOW(), NOW()),

-- GIGABYTE Mainboards
(gen_random_uuid(), 'GIGABYTE Z790 AORUS Elite AX', 'Bo mạch chủ GIGABYTE Z790 AORUS Elite, socket LGA1700, DDR5, ATX, WiFi 6E', 6990000, 28,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'GIGABYTE'),
 (SELECT id FROM tbl_product_types WHERE name = 'MAINBOARD'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'GIGABYTE B650 AORUS Elite AX', 'Bo mạch chủ GIGABYTE B650 AORUS Elite, socket AM5, DDR5, ATX, WiFi 6E', 5490000, 32,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'GIGABYTE'),
 (SELECT id FROM tbl_product_types WHERE name = 'MAINBOARD'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'GIGABYTE B550M DS3H', 'Bo mạch chủ GIGABYTE B550M DS3H, socket AM4, DDR4, mATX, giá rẻ cho Ryzen 5000', 2190000, 55,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'GIGABYTE'),
 (SELECT id FROM tbl_product_types WHERE name = 'MAINBOARD'),
 36, 'ACTIVE', NOW(), NOW());


-- =====================================================
-- 3. RAM - Bộ nhớ trong
-- =====================================================
INSERT INTO tbl_products (id, name, description, price, stock, category_id, brand_id, product_type_id, warranty_period, status, created_at, updated_at)
VALUES
-- Kingston RAM
(gen_random_uuid(), 'Kingston Fury Beast DDR5 32GB (2x16GB) 5600MHz', 'RAM Kingston Fury Beast DDR5 32GB kit, 5600MHz, CL40, XMP 3.0, tản nhiệt nhôm', 2890000, 50,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Kingston'),
 (SELECT id FROM tbl_product_types WHERE name = 'RAM'),
 60, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Kingston Fury Beast DDR4 32GB (2x16GB) 3200MHz', 'RAM Kingston Fury Beast DDR4 32GB kit, 3200MHz, CL16, XMP 2.0', 1890000, 65,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Kingston'),
 (SELECT id FROM tbl_product_types WHERE name = 'RAM'),
 60, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Kingston Fury Beast DDR4 16GB (2x8GB) 3200MHz', 'RAM Kingston Fury Beast DDR4 16GB kit, 3200MHz, CL16, XMP 2.0', 990000, 80,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Kingston'),
 (SELECT id FROM tbl_product_types WHERE name = 'RAM'),
 60, 'ACTIVE', NOW(), NOW()),

-- Corsair RAM
(gen_random_uuid(), 'Corsair Vengeance DDR5 32GB (2x16GB) 6000MHz', 'RAM Corsair Vengeance DDR5 32GB kit, 6000MHz, CL36, XMP 3.0, Intel XMP tối ưu', 3290000, 40,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Corsair'),
 (SELECT id FROM tbl_product_types WHERE name = 'RAM'),
 60, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Corsair Vengeance RGB DDR5 64GB (2x32GB) 5600MHz', 'RAM Corsair Vengeance RGB DDR5 64GB kit, 5600MHz, LED RGB, XMP 3.0', 5990000, 25,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Corsair'),
 (SELECT id FROM tbl_product_types WHERE name = 'RAM'),
 60, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Corsair Vengeance LPX DDR4 32GB (2x16GB) 3600MHz', 'RAM Corsair Vengeance LPX DDR4 32GB kit, 3600MHz, CL18, profile thấp', 2190000, 55,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Corsair'),
 (SELECT id FROM tbl_product_types WHERE name = 'RAM'),
 60, 'ACTIVE', NOW(), NOW());


-- =====================================================
-- 4. GPU - Card đồ họa
-- =====================================================
INSERT INTO tbl_products (id, name, description, price, stock, category_id, brand_id, product_type_id, warranty_period, status, created_at, updated_at)
VALUES
-- NVIDIA GPUs (ASUS)
(gen_random_uuid(), 'ASUS ROG STRIX GeForce RTX 4090 OC 24GB', 'Card đồ họa ASUS ROG STRIX RTX 4090 OC, 24GB GDDR6X, 3 fan, RGB, hiệu năng cao nhất', 49990000, 10,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'ASUS'),
 (SELECT id FROM tbl_product_types WHERE name = 'GPU'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'ASUS TUF Gaming GeForce RTX 4070 Ti SUPER 16GB', 'Card đồ họa ASUS TUF RTX 4070 Ti SUPER, 16GB GDDR6X, 3 fan, bền bỉ', 22990000, 18,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'ASUS'),
 (SELECT id FROM tbl_product_types WHERE name = 'GPU'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'ASUS Dual GeForce RTX 4060 Ti 8GB', 'Card đồ họa ASUS Dual RTX 4060 Ti, 8GB GDDR6, 2 fan, hiệu năng/giá tốt', 10990000, 30,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'ASUS'),
 (SELECT id FROM tbl_product_types WHERE name = 'GPU'),
 36, 'ACTIVE', NOW(), NOW()),

-- NVIDIA GPUs (MSI)
(gen_random_uuid(), 'MSI GeForce RTX 4080 SUPER Gaming X Trio 16GB', 'Card đồ họa MSI RTX 4080 SUPER Gaming X Trio, 16GB GDDR6X, 3 fan, RGB', 32990000, 12,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'MSI'),
 (SELECT id FROM tbl_product_types WHERE name = 'GPU'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'MSI GeForce RTX 4070 SUPER Ventus 2X 12GB', 'Card đồ họa MSI RTX 4070 SUPER Ventus, 12GB GDDR6X, 2 fan, compact', 15990000, 25,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'MSI'),
 (SELECT id FROM tbl_product_types WHERE name = 'GPU'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'MSI GeForce RTX 4060 Ventus 2X Black 8GB', 'Card đồ họa MSI RTX 4060 Ventus, 8GB GDDR6, 2 fan, giá tốt cho 1080p gaming', 7990000, 40,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'MSI'),
 (SELECT id FROM tbl_product_types WHERE name = 'GPU'),
 36, 'ACTIVE', NOW(), NOW()),

-- NVIDIA GPUs (GIGABYTE)
(gen_random_uuid(), 'GIGABYTE GeForce RTX 4070 Ti SUPER Gaming OC 16GB', 'Card đồ họa GIGABYTE RTX 4070 Ti SUPER Gaming OC, 16GB GDDR6X, WindForce 3X', 21990000, 20,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'GIGABYTE'),
 (SELECT id FROM tbl_product_types WHERE name = 'GPU'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'GIGABYTE GeForce RTX 4060 Eagle OC 8GB', 'Card đồ họa GIGABYTE RTX 4060 Eagle OC, 8GB GDDR6, 2 fan, compact design', 7490000, 45,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'GIGABYTE'),
 (SELECT id FROM tbl_product_types WHERE name = 'GPU'),
 36, 'ACTIVE', NOW(), NOW()),

-- AMD GPUs
(gen_random_uuid(), 'ASUS TUF Gaming Radeon RX 7900 XTX OC 24GB', 'Card đồ họa ASUS TUF RX 7900 XTX, 24GB GDDR6, 3 fan, đối thủ RTX 4080', 26990000, 15,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'ASUS'),
 (SELECT id FROM tbl_product_types WHERE name = 'GPU'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'MSI Radeon RX 7800 XT Gaming Trio Classic 16GB', 'Card đồ họa MSI RX 7800 XT, 16GB GDDR6, 3 fan, 1440p gaming tuyệt vời', 13990000, 22,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'MSI'),
 (SELECT id FROM tbl_product_types WHERE name = 'GPU'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'GIGABYTE Radeon RX 7600 Gaming OC 8GB', 'Card đồ họa GIGABYTE RX 7600 Gaming OC, 8GB GDDR6, giá tốt cho 1080p', 6990000, 35,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'GIGABYTE'),
 (SELECT id FROM tbl_product_types WHERE name = 'GPU'),
 36, 'ACTIVE', NOW(), NOW());


-- =====================================================
-- 5. PSU - Nguồn máy tính
-- =====================================================
INSERT INTO tbl_products (id, name, description, price, stock, category_id, brand_id, product_type_id, warranty_period, status, created_at, updated_at)
VALUES
-- Corsair PSU
(gen_random_uuid(), 'Corsair RM1000x 1000W 80+ Gold', 'Nguồn Corsair RM1000x, 1000W, 80+ Gold, Full Modular, quạt 135mm Zero RPM', 4290000, 25,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Corsair'),
 (SELECT id FROM tbl_product_types WHERE name = 'PSU'),
 84, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Corsair RM850x 850W 80+ Gold', 'Nguồn Corsair RM850x, 850W, 80+ Gold, Full Modular, ATX 3.0, PCIe 5.0 ready', 3490000, 35,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Corsair'),
 (SELECT id FROM tbl_product_types WHERE name = 'PSU'),
 84, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Corsair CV650 650W 80+ Bronze', 'Nguồn Corsair CV650, 650W, 80+ Bronze, Non-Modular, giá tốt', 1290000, 50,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Corsair'),
 (SELECT id FROM tbl_product_types WHERE name = 'PSU'),
 36, 'ACTIVE', NOW(), NOW()),

-- Cooler Master PSU
(gen_random_uuid(), 'Cooler Master V850 Gold V2 850W', 'Nguồn Cooler Master V850 Gold V2, 850W, 80+ Gold, Full Modular, quạt FDB 135mm', 2990000, 30,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Cooler Master'),
 (SELECT id FROM tbl_product_types WHERE name = 'PSU'),
 60, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Cooler Master MWE Gold 750 V2 750W', 'Nguồn Cooler Master MWE Gold 750 V2, 750W, 80+ Gold, Full Modular', 2190000, 40,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Cooler Master'),
 (SELECT id FROM tbl_product_types WHERE name = 'PSU'),
 60, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Cooler Master Elite V3 600W', 'Nguồn Cooler Master Elite V3, 600W, 80+ White, Non-Modular, giá rẻ', 790000, 60,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Cooler Master'),
 (SELECT id FROM tbl_product_types WHERE name = 'PSU'),
 24, 'ACTIVE', NOW(), NOW());


-- =====================================================
-- 6. CASE - Vỏ máy tính
-- =====================================================
INSERT INTO tbl_products (id, name, description, price, stock, category_id, brand_id, product_type_id, warranty_period, status, created_at, updated_at)
VALUES
-- Corsair Cases
(gen_random_uuid(), 'Corsair 5000D Airflow Tempered Glass', 'Vỏ case Corsair 5000D Airflow, Mid-Tower, kính cường lực, airflow tối ưu, ATX', 3990000, 20,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Corsair'),
 (SELECT id FROM tbl_product_types WHERE name = 'CASE'),
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Corsair 4000D Airflow Tempered Glass', 'Vỏ case Corsair 4000D Airflow, Mid-Tower, kính cường lực, compact, ATX', 2490000, 30,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Corsair'),
 (SELECT id FROM tbl_product_types WHERE name = 'CASE'),
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Corsair iCUE 4000X RGB Tempered Glass', 'Vỏ case Corsair iCUE 4000X RGB, Mid-Tower, 3 fan RGB, kính cường lực', 3290000, 25,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Corsair'),
 (SELECT id FROM tbl_product_types WHERE name = 'CASE'),
 24, 'ACTIVE', NOW(), NOW()),

-- Cooler Master Cases
(gen_random_uuid(), 'Cooler Master MasterBox TD500 Mesh', 'Vỏ case Cooler Master TD500 Mesh, Mid-Tower, mesh front, 3 fan ARGB, ATX', 2290000, 35,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Cooler Master'),
 (SELECT id FROM tbl_product_types WHERE name = 'CASE'),
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Cooler Master NR200P Max', 'Vỏ case Cooler Master NR200P Max, Mini-ITX, kèm PSU 850W + AIO 280mm', 7990000, 12,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Cooler Master'),
 (SELECT id FROM tbl_product_types WHERE name = 'CASE'),
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Cooler Master MasterBox Q300L', 'Vỏ case Cooler Master Q300L, Micro-ATX, compact, magnetic dust filter', 990000, 45,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Cooler Master'),
 (SELECT id FROM tbl_product_types WHERE name = 'CASE'),
 24, 'ACTIVE', NOW(), NOW());


-- =====================================================
-- 7. STORAGE - Ổ cứng SSD/HDD
-- =====================================================
INSERT INTO tbl_products (id, name, description, price, stock, category_id, brand_id, product_type_id, warranty_period, status, created_at, updated_at)
VALUES
-- Samsung SSD
(gen_random_uuid(), 'Samsung 990 Pro 2TB NVMe M.2', 'SSD Samsung 990 Pro 2TB, NVMe PCIe 4.0, đọc 7450MB/s, ghi 6900MB/s, có heatsink', 5490000, 30,
 (SELECT id FROM tbl_categories WHERE name = 'Lưu trữ'),
 (SELECT id FROM tbl_brands WHERE name = 'Samsung'),
 (SELECT id FROM tbl_product_types WHERE name = 'STORAGE'),
 60, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Samsung 980 Pro 1TB NVMe M.2', 'SSD Samsung 980 Pro 1TB, NVMe PCIe 4.0, đọc 7000MB/s, ghi 5000MB/s', 2990000, 45,
 (SELECT id FROM tbl_categories WHERE name = 'Lưu trữ'),
 (SELECT id FROM tbl_brands WHERE name = 'Samsung'),
 (SELECT id FROM tbl_product_types WHERE name = 'STORAGE'),
 60, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Samsung 870 EVO 1TB SATA', 'SSD Samsung 870 EVO 1TB, SATA 2.5 inch, đọc 560MB/s, ghi 530MB/s', 2290000, 50,
 (SELECT id FROM tbl_categories WHERE name = 'Lưu trữ'),
 (SELECT id FROM tbl_brands WHERE name = 'Samsung'),
 (SELECT id FROM tbl_product_types WHERE name = 'STORAGE'),
 60, 'ACTIVE', NOW(), NOW()),

-- Kingston SSD
(gen_random_uuid(), 'Kingston KC3000 2TB NVMe M.2', 'SSD Kingston KC3000 2TB, NVMe PCIe 4.0, đọc 7000MB/s, ghi 7000MB/s', 4290000, 25,
 (SELECT id FROM tbl_categories WHERE name = 'Lưu trữ'),
 (SELECT id FROM tbl_brands WHERE name = 'Kingston'),
 (SELECT id FROM tbl_product_types WHERE name = 'STORAGE'),
 60, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Kingston NV2 1TB NVMe M.2', 'SSD Kingston NV2 1TB, NVMe PCIe 4.0, đọc 3500MB/s, giá tốt', 1490000, 60,
 (SELECT id FROM tbl_categories WHERE name = 'Lưu trữ'),
 (SELECT id FROM tbl_brands WHERE name = 'Kingston'),
 (SELECT id FROM tbl_product_types WHERE name = 'STORAGE'),
 36, 'ACTIVE', NOW(), NOW()),

-- Western Digital
(gen_random_uuid(), 'WD Black SN850X 2TB NVMe M.2', 'SSD WD Black SN850X 2TB, NVMe PCIe 4.0, đọc 7300MB/s, gaming SSD', 4990000, 28,
 (SELECT id FROM tbl_categories WHERE name = 'Lưu trữ'),
 (SELECT id FROM tbl_brands WHERE name = 'Western Digital'),
 (SELECT id FROM tbl_product_types WHERE name = 'STORAGE'),
 60, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'WD Blue SN580 1TB NVMe M.2', 'SSD WD Blue SN580 1TB, NVMe PCIe 4.0, đọc 4150MB/s, giá tốt', 1790000, 55,
 (SELECT id FROM tbl_categories WHERE name = 'Lưu trữ'),
 (SELECT id FROM tbl_brands WHERE name = 'Western Digital'),
 (SELECT id FROM tbl_product_types WHERE name = 'STORAGE'),
 60, 'ACTIVE', NOW(), NOW()),

-- Seagate HDD
(gen_random_uuid(), 'Seagate Barracuda 4TB 3.5" HDD', 'HDD Seagate Barracuda 4TB, 3.5 inch, 5400RPM, 256MB cache, lưu trữ lớn', 2490000, 40,
 (SELECT id FROM tbl_categories WHERE name = 'Lưu trữ'),
 (SELECT id FROM tbl_brands WHERE name = 'Seagate'),
 (SELECT id FROM tbl_product_types WHERE name = 'STORAGE'),
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Seagate Barracuda 2TB 3.5" HDD', 'HDD Seagate Barracuda 2TB, 3.5 inch, 7200RPM, 256MB cache', 1490000, 55,
 (SELECT id FROM tbl_categories WHERE name = 'Lưu trữ'),
 (SELECT id FROM tbl_brands WHERE name = 'Seagate'),
 (SELECT id FROM tbl_product_types WHERE name = 'STORAGE'),
 24, 'ACTIVE', NOW(), NOW());


-- =====================================================
-- 8. COOLER - Tản nhiệt CPU
-- =====================================================
INSERT INTO tbl_products (id, name, description, price, stock, category_id, brand_id, product_type_id, warranty_period, status, created_at, updated_at)
VALUES
-- Corsair Coolers
(gen_random_uuid(), 'Corsair iCUE H150i Elite LCD XT 360mm', 'Tản nhiệt nước Corsair H150i Elite LCD, 360mm, màn hình LCD 2.1 inch, RGB', 7990000, 15,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Corsair'),
 (SELECT id FROM tbl_product_types WHERE name = 'COOLER'),
 60, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Corsair iCUE H100i RGB Elite 240mm', 'Tản nhiệt nước Corsair H100i RGB Elite, 240mm, 2 fan ML RGB, hiệu năng cao', 3990000, 25,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Corsair'),
 (SELECT id FROM tbl_product_types WHERE name = 'COOLER'),
 60, 'ACTIVE', NOW(), NOW()),

-- Cooler Master Coolers
(gen_random_uuid(), 'Cooler Master MasterLiquid ML360 Illusion', 'Tản nhiệt nước Cooler Master ML360 Illusion, 360mm, 3 fan ARGB, pump Gen 3', 4290000, 20,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Cooler Master'),
 (SELECT id FROM tbl_product_types WHERE name = 'COOLER'),
 60, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Cooler Master Hyper 212 Halo Black', 'Tản nhiệt khí Cooler Master Hyper 212 Halo, tower cooler, fan ARGB, giá tốt', 990000, 45,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Cooler Master'),
 (SELECT id FROM tbl_product_types WHERE name = 'COOLER'),
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Cooler Master Hyper 212 Black Edition', 'Tản nhiệt khí Cooler Master Hyper 212 Black, tower cooler, 4 ống đồng, classic', 790000, 55,
 (SELECT id FROM tbl_categories WHERE name = 'Linh kiện máy tính'),
 (SELECT id FROM tbl_brands WHERE name = 'Cooler Master'),
 (SELECT id FROM tbl_product_types WHERE name = 'COOLER'),
 24, 'ACTIVE', NOW(), NOW());


-- =====================================================
-- 9. MÀN HÌNH - Monitor
-- =====================================================
INSERT INTO tbl_products (id, name, description, price, stock, category_id, brand_id, product_type_id, warranty_period, status, created_at, updated_at)
VALUES
-- ASUS Monitors
(gen_random_uuid(), 'ASUS ROG Swift PG27AQN 27" 360Hz', 'Màn hình ASUS ROG Swift PG27AQN, 27 inch, 2K QHD, IPS, 360Hz, 1ms, G-Sync', 24990000, 8,
 (SELECT id FROM tbl_categories WHERE name = 'Màn hình'),
 (SELECT id FROM tbl_brands WHERE name = 'ASUS'),
 (SELECT id FROM tbl_product_types WHERE name = 'Màn Hình'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'ASUS TUF Gaming VG27AQ1A 27" 170Hz', 'Màn hình ASUS TUF VG27AQ1A, 27 inch, 2K QHD, IPS, 170Hz, 1ms, HDR10', 7990000, 25,
 (SELECT id FROM tbl_categories WHERE name = 'Màn hình'),
 (SELECT id FROM tbl_brands WHERE name = 'ASUS'),
 (SELECT id FROM tbl_product_types WHERE name = 'Màn Hình'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'ASUS ProArt PA278CV 27" 4K', 'Màn hình ASUS ProArt PA278CV, 27 inch, 4K UHD, IPS, 100% sRGB, USB-C, cho designer', 9990000, 15,
 (SELECT id FROM tbl_categories WHERE name = 'Màn hình'),
 (SELECT id FROM tbl_brands WHERE name = 'ASUS'),
 (SELECT id FROM tbl_product_types WHERE name = 'Màn Hình'),
 36, 'ACTIVE', NOW(), NOW()),

-- Samsung Monitors
(gen_random_uuid(), 'Samsung Odyssey G9 49" 240Hz', 'Màn hình Samsung Odyssey G9, 49 inch, Dual QHD, VA cong 1000R, 240Hz, HDR1000', 32990000, 6,
 (SELECT id FROM tbl_categories WHERE name = 'Màn hình'),
 (SELECT id FROM tbl_brands WHERE name = 'Samsung'),
 (SELECT id FROM tbl_product_types WHERE name = 'Màn Hình'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Samsung Odyssey G7 32" 240Hz', 'Màn hình Samsung Odyssey G7, 32 inch, 2K QHD, VA cong 1000R, 240Hz, 1ms', 14990000, 18,
 (SELECT id FROM tbl_categories WHERE name = 'Màn hình'),
 (SELECT id FROM tbl_brands WHERE name = 'Samsung'),
 (SELECT id FROM tbl_product_types WHERE name = 'Màn Hình'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Samsung LS27BG400 27" 240Hz', 'Màn hình Samsung LS27BG400, 27 inch, Full HD, IPS, 240Hz, 1ms, giá tốt', 5990000, 30,
 (SELECT id FROM tbl_categories WHERE name = 'Màn hình'),
 (SELECT id FROM tbl_brands WHERE name = 'Samsung'),
 (SELECT id FROM tbl_product_types WHERE name = 'Màn Hình'),
 36, 'ACTIVE', NOW(), NOW()),

-- Dell Monitors
(gen_random_uuid(), 'Dell Alienware AW3423DWF 34" QD-OLED', 'Màn hình Dell Alienware AW3423DWF, 34 inch, QD-OLED, 3440x1440, 165Hz, HDR', 27990000, 10,
 (SELECT id FROM tbl_categories WHERE name = 'Màn hình'),
 (SELECT id FROM tbl_brands WHERE name = 'Dell'),
 (SELECT id FROM tbl_product_types WHERE name = 'Màn Hình'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Dell UltraSharp U2723QE 27" 4K', 'Màn hình Dell UltraSharp U2723QE, 27 inch, 4K UHD, IPS Black, USB-C 90W, cho văn phòng', 15990000, 12,
 (SELECT id FROM tbl_categories WHERE name = 'Màn hình'),
 (SELECT id FROM tbl_brands WHERE name = 'Dell'),
 (SELECT id FROM tbl_product_types WHERE name = 'Màn Hình'),
 36, 'ACTIVE', NOW(), NOW()),

-- LG Monitors
(gen_random_uuid(), 'LG UltraGear 27GP850-B 27" 180Hz', 'Màn hình LG UltraGear 27GP850-B, 27 inch, 2K QHD, Nano IPS, 180Hz, 1ms, HDR400', 8990000, 22,
 (SELECT id FROM tbl_categories WHERE name = 'Màn hình'),
 (SELECT id FROM tbl_brands WHERE name = 'Lenovo'),
 (SELECT id FROM tbl_product_types WHERE name = 'Màn Hình'),
 36, 'ACTIVE', NOW(), NOW()),

-- ViewSonic Monitors
(gen_random_uuid(), 'ViewSonic VX2758-2KP-MHD 27" 144Hz', 'Màn hình ViewSonic VX2758-2KP-MHD, 27 inch, 2K QHD, IPS, 144Hz, 1ms, giá tốt', 5490000, 35,
 (SELECT id FROM tbl_categories WHERE name = 'Màn hình'),
 (SELECT id FROM tbl_brands WHERE name = 'Viewsonic'),
 (SELECT id FROM tbl_product_types WHERE name = 'Màn Hình'),
 36, 'ACTIVE', NOW(), NOW());


-- =====================================================
-- 10. LAPTOP
-- =====================================================
INSERT INTO tbl_products (id, name, description, price, stock, category_id, brand_id, product_type_id, warranty_period, status, created_at, updated_at)
VALUES
-- ASUS Laptops
(gen_random_uuid(), 'ASUS ROG Strix G16 G614JV', 'Laptop gaming ASUS ROG Strix G16, Intel i7-13650HX, RTX 4060, 16GB DDR5, 512GB SSD, 16" 165Hz', 35990000, 12,
 (SELECT id FROM tbl_categories WHERE name = 'Laptop'),
 (SELECT id FROM tbl_brands WHERE name = 'ASUS'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'ASUS TUF Gaming F15 FX507ZC4', 'Laptop gaming ASUS TUF F15, Intel i5-12500H, RTX 3050, 8GB DDR4, 512GB SSD, 15.6" 144Hz', 19990000, 20,
 (SELECT id FROM tbl_categories WHERE name = 'Laptop'),
 (SELECT id FROM tbl_brands WHERE name = 'ASUS'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'ASUS Vivobook 15 OLED M1505YA', 'Laptop ASUS Vivobook 15 OLED, AMD Ryzen 7 7730U, 16GB, 512GB SSD, 15.6" OLED FHD', 17990000, 25,
 (SELECT id FROM tbl_categories WHERE name = 'Laptop'),
 (SELECT id FROM tbl_brands WHERE name = 'ASUS'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

-- MSI Laptops
(gen_random_uuid(), 'MSI Raider GE78 HX 13VH', 'Laptop gaming MSI Raider GE78 HX, Intel i9-13980HX, RTX 4080, 32GB DDR5, 2TB SSD, 17" 240Hz', 79990000, 5,
 (SELECT id FROM tbl_categories WHERE name = 'Laptop'),
 (SELECT id FROM tbl_brands WHERE name = 'MSI'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'MSI Katana 15 B13VFK', 'Laptop gaming MSI Katana 15, Intel i7-13620H, RTX 4060, 16GB DDR5, 512GB SSD, 15.6" 144Hz', 29990000, 15,
 (SELECT id FROM tbl_categories WHERE name = 'Laptop'),
 (SELECT id FROM tbl_brands WHERE name = 'MSI'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'MSI Modern 14 C12M', 'Laptop MSI Modern 14, Intel i5-1235U, Intel Iris Xe, 8GB, 512GB SSD, 14" FHD, mỏng nhẹ', 13990000, 30,
 (SELECT id FROM tbl_categories WHERE name = 'Laptop'),
 (SELECT id FROM tbl_brands WHERE name = 'MSI'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

-- Lenovo Laptops
(gen_random_uuid(), 'Lenovo Legion Pro 5 16IRX8', 'Laptop gaming Lenovo Legion Pro 5, Intel i7-13700HX, RTX 4070, 16GB DDR5, 1TB SSD, 16" 240Hz', 45990000, 10,
 (SELECT id FROM tbl_categories WHERE name = 'Laptop'),
 (SELECT id FROM tbl_brands WHERE name = 'Lenovo'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Lenovo IdeaPad Gaming 3 15IAH7', 'Laptop gaming Lenovo IdeaPad Gaming 3, Intel i5-12500H, RTX 3050, 8GB, 512GB SSD, 15.6" 120Hz', 17990000, 22,
 (SELECT id FROM tbl_categories WHERE name = 'Laptop'),
 (SELECT id FROM tbl_brands WHERE name = 'Lenovo'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Lenovo ThinkPad E14 Gen 5', 'Laptop Lenovo ThinkPad E14 Gen 5, Intel i5-1335U, 16GB, 512GB SSD, 14" FHD, doanh nhân', 18990000, 18,
 (SELECT id FROM tbl_categories WHERE name = 'Laptop'),
 (SELECT id FROM tbl_brands WHERE name = 'Lenovo'),
 NULL,
 36, 'ACTIVE', NOW(), NOW()),

-- Dell Laptops
(gen_random_uuid(), 'Dell Alienware m16 R1', 'Laptop gaming Dell Alienware m16, Intel i7-13700HX, RTX 4070, 16GB DDR5, 1TB SSD, 16" 240Hz', 52990000, 8,
 (SELECT id FROM tbl_categories WHERE name = 'Laptop'),
 (SELECT id FROM tbl_brands WHERE name = 'Dell'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Dell G15 5530', 'Laptop gaming Dell G15 5530, Intel i7-13650HX, RTX 4060, 16GB DDR5, 512GB SSD, 15.6" 165Hz', 29990000, 16,
 (SELECT id FROM tbl_categories WHERE name = 'Laptop'),
 (SELECT id FROM tbl_brands WHERE name = 'Dell'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Dell Inspiron 15 3520', 'Laptop Dell Inspiron 15 3520, Intel i5-1235U, 8GB, 512GB SSD, 15.6" FHD, văn phòng', 12990000, 35,
 (SELECT id FROM tbl_categories WHERE name = 'Laptop'),
 (SELECT id FROM tbl_brands WHERE name = 'Dell'),
 NULL,
 12, 'ACTIVE', NOW(), NOW()),

-- HP Laptops
(gen_random_uuid(), 'HP OMEN 16-wf0076TX', 'Laptop gaming HP OMEN 16, Intel i7-13700HX, RTX 4060, 16GB DDR5, 512GB SSD, 16.1" 165Hz', 34990000, 12,
 (SELECT id FROM tbl_categories WHERE name = 'Laptop'),
 (SELECT id FROM tbl_brands WHERE name = 'HP'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'HP Victus 15-fa1093TX', 'Laptop gaming HP Victus 15, Intel i5-12500H, RTX 4050, 8GB, 512GB SSD, 15.6" 144Hz', 19990000, 20,
 (SELECT id FROM tbl_categories WHERE name = 'Laptop'),
 (SELECT id FROM tbl_brands WHERE name = 'HP'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'HP 15s-fq5078TU', 'Laptop HP 15s, Intel i5-1235U, 8GB, 512GB SSD, 15.6" FHD, văn phòng học tập', 11990000, 40,
 (SELECT id FROM tbl_categories WHERE name = 'Laptop'),
 (SELECT id FROM tbl_brands WHERE name = 'HP'),
 NULL,
 12, 'ACTIVE', NOW(), NOW()),

-- Acer Laptops
(gen_random_uuid(), 'Acer Predator Helios 16 PH16-71', 'Laptop gaming Acer Predator Helios 16, Intel i9-13900HX, RTX 4080, 32GB DDR5, 1TB SSD, 16" 240Hz', 69990000, 6,
 (SELECT id FROM tbl_categories WHERE name = 'Laptop'),
 (SELECT id FROM tbl_brands WHERE name = 'ACER'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Acer Nitro 5 AN515-58', 'Laptop gaming Acer Nitro 5, Intel i5-12500H, RTX 4050, 8GB, 512GB SSD, 15.6" 144Hz', 18990000, 25,
 (SELECT id FROM tbl_categories WHERE name = 'Laptop'),
 (SELECT id FROM tbl_brands WHERE name = 'ACER'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Acer Aspire 5 A515-58M', 'Laptop Acer Aspire 5, Intel i5-1335U, 16GB, 512GB SSD, 15.6" FHD, đa năng', 14990000, 30,
 (SELECT id FROM tbl_categories WHERE name = 'Laptop'),
 (SELECT id FROM tbl_brands WHERE name = 'ACER'),
 NULL,
 24, 'ACTIVE', NOW(), NOW());


-- =====================================================
-- 11. GAMING GEAR - Phụ kiện Gaming
-- =====================================================
INSERT INTO tbl_products (id, name, description, price, stock, category_id, brand_id, product_type_id, warranty_period, status, created_at, updated_at)
VALUES
-- Logitech Gaming Gear
(gen_random_uuid(), 'Logitech G Pro X Superlight 2', 'Chuột gaming Logitech G Pro X Superlight 2, wireless, 32000 DPI, 60g siêu nhẹ, HERO 2 sensor', 3690000, 30,
 (SELECT id FROM tbl_categories WHERE name = 'Gaming Gear'),
 (SELECT id FROM tbl_brands WHERE name = 'Logitech'),
 (SELECT id FROM tbl_product_types WHERE name = 'Gaming Gear'),
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Logitech G502 X Plus', 'Chuột gaming Logitech G502 X Plus, wireless, 25600 DPI, LIGHTFORCE switches, RGB', 3290000, 35,
 (SELECT id FROM tbl_categories WHERE name = 'Gaming Gear'),
 (SELECT id FROM tbl_brands WHERE name = 'Logitech'),
 (SELECT id FROM tbl_product_types WHERE name = 'Gaming Gear'),
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Logitech G Pro X TKL Lightspeed', 'Bàn phím gaming Logitech G Pro X TKL, wireless, GX switches, RGB, TKL compact', 3990000, 25,
 (SELECT id FROM tbl_categories WHERE name = 'Gaming Gear'),
 (SELECT id FROM tbl_brands WHERE name = 'Logitech'),
 (SELECT id FROM tbl_product_types WHERE name = 'Gaming Gear'),
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Logitech G733 Lightspeed', 'Tai nghe gaming Logitech G733, wireless, 7.1 surround, RGB, 278g nhẹ', 2990000, 28,
 (SELECT id FROM tbl_categories WHERE name = 'Gaming Gear'),
 (SELECT id FROM tbl_brands WHERE name = 'Logitech'),
 (SELECT id FROM tbl_product_types WHERE name = 'Gaming Gear'),
 24, 'ACTIVE', NOW(), NOW()),

-- Razer Gaming Gear
(gen_random_uuid(), 'Razer DeathAdder V3 Pro', 'Chuột gaming Razer DeathAdder V3 Pro, wireless, 30000 DPI, Focus Pro sensor, 63g', 3490000, 32,
 (SELECT id FROM tbl_categories WHERE name = 'Gaming Gear'),
 (SELECT id FROM tbl_brands WHERE name = 'Razer'),
 (SELECT id FROM tbl_product_types WHERE name = 'Gaming Gear'),
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Razer Viper V2 Pro', 'Chuột gaming Razer Viper V2 Pro, wireless, 30000 DPI, 58g siêu nhẹ, esports', 3290000, 28,
 (SELECT id FROM tbl_categories WHERE name = 'Gaming Gear'),
 (SELECT id FROM tbl_brands WHERE name = 'Razer'),
 (SELECT id FROM tbl_product_types WHERE name = 'Gaming Gear'),
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Razer Huntsman V3 Pro TKL', 'Bàn phím gaming Razer Huntsman V3 Pro TKL, analog optical switches, RGB, TKL', 5490000, 18,
 (SELECT id FROM tbl_categories WHERE name = 'Gaming Gear'),
 (SELECT id FROM tbl_brands WHERE name = 'Razer'),
 (SELECT id FROM tbl_product_types WHERE name = 'Gaming Gear'),
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Razer BlackShark V2 Pro', 'Tai nghe gaming Razer BlackShark V2 Pro, wireless, THX Spatial Audio, 70 giờ pin', 4290000, 22,
 (SELECT id FROM tbl_categories WHERE name = 'Gaming Gear'),
 (SELECT id FROM tbl_brands WHERE name = 'Razer'),
 (SELECT id FROM tbl_product_types WHERE name = 'Gaming Gear'),
 24, 'ACTIVE', NOW(), NOW()),

-- Corsair Gaming Gear
(gen_random_uuid(), 'Corsair Dark Core RGB Pro SE', 'Chuột gaming Corsair Dark Core RGB Pro SE, wireless, 18000 DPI, Qi charging', 2490000, 25,
 (SELECT id FROM tbl_categories WHERE name = 'Gaming Gear'),
 (SELECT id FROM tbl_brands WHERE name = 'Corsair'),
 (SELECT id FROM tbl_product_types WHERE name = 'Gaming Gear'),
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Corsair K70 RGB Pro', 'Bàn phím gaming Corsair K70 RGB Pro, Cherry MX switches, RGB, full-size, nhôm', 3790000, 20,
 (SELECT id FROM tbl_categories WHERE name = 'Gaming Gear'),
 (SELECT id FROM tbl_brands WHERE name = 'Corsair'),
 (SELECT id FROM tbl_product_types WHERE name = 'Gaming Gear'),
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Corsair Virtuoso RGB Wireless XT', 'Tai nghe gaming Corsair Virtuoso RGB Wireless XT, Bluetooth + 2.4GHz, Hi-Res Audio', 5990000, 15,
 (SELECT id FROM tbl_categories WHERE name = 'Gaming Gear'),
 (SELECT id FROM tbl_brands WHERE name = 'Corsair'),
 (SELECT id FROM tbl_product_types WHERE name = 'Gaming Gear'),
 24, 'ACTIVE', NOW(), NOW()),

-- HyperX Gaming Gear
(gen_random_uuid(), 'HyperX Pulsefire Haste 2 Wireless', 'Chuột gaming HyperX Pulsefire Haste 2, wireless, 26000 DPI, 61g siêu nhẹ', 1990000, 40,
 (SELECT id FROM tbl_categories WHERE name = 'Gaming Gear'),
 (SELECT id FROM tbl_brands WHERE name = 'HyperX'),
 (SELECT id FROM tbl_product_types WHERE name = 'Gaming Gear'),
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'HyperX Alloy Origins 65', 'Bàn phím gaming HyperX Alloy Origins 65, HyperX switches, RGB, 65% compact', 2290000, 30,
 (SELECT id FROM tbl_categories WHERE name = 'Gaming Gear'),
 (SELECT id FROM tbl_brands WHERE name = 'HyperX'),
 (SELECT id FROM tbl_product_types WHERE name = 'Gaming Gear'),
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'HyperX Cloud III Wireless', 'Tai nghe gaming HyperX Cloud III Wireless, 2.4GHz, DTS Headphone:X, 120 giờ pin', 3490000, 25,
 (SELECT id FROM tbl_categories WHERE name = 'Gaming Gear'),
 (SELECT id FROM tbl_brands WHERE name = 'HyperX'),
 (SELECT id FROM tbl_product_types WHERE name = 'Gaming Gear'),
 24, 'ACTIVE', NOW(), NOW());


-- =====================================================
-- 12. PHỤ KIỆN - Chuột, bàn phím văn phòng
-- =====================================================
INSERT INTO tbl_products (id, name, description, price, stock, category_id, brand_id, product_type_id, warranty_period, status, created_at, updated_at)
VALUES
-- Logitech Office
(gen_random_uuid(), 'Logitech MX Master 3S', 'Chuột văn phòng Logitech MX Master 3S, wireless, 8000 DPI, MagSpeed scroll, USB-C', 2490000, 35,
 (SELECT id FROM tbl_categories WHERE name = 'Phụ kiện'),
 (SELECT id FROM tbl_brands WHERE name = 'Logitech'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Logitech MX Keys S', 'Bàn phím văn phòng Logitech MX Keys S, wireless, backlit, Smart Actions, USB-C', 2690000, 30,
 (SELECT id FROM tbl_categories WHERE name = 'Phụ kiện'),
 (SELECT id FROM tbl_brands WHERE name = 'Logitech'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Logitech M650 Signature', 'Chuột văn phòng Logitech M650, wireless, SmartWheel, silent clicks, giá tốt', 690000, 60,
 (SELECT id FROM tbl_categories WHERE name = 'Phụ kiện'),
 (SELECT id FROM tbl_brands WHERE name = 'Logitech'),
 NULL,
 12, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Logitech K380 Multi-Device', 'Bàn phím Logitech K380, Bluetooth, kết nối 3 thiết bị, compact, nhiều màu', 790000, 55,
 (SELECT id FROM tbl_categories WHERE name = 'Phụ kiện'),
 (SELECT id FROM tbl_brands WHERE name = 'Logitech'),
 NULL,
 12, 'ACTIVE', NOW(), NOW()),

-- Webcam
(gen_random_uuid(), 'Logitech C920 HD Pro', 'Webcam Logitech C920 HD Pro, 1080p 30fps, autofocus, dual mic, streaming', 1890000, 40,
 (SELECT id FROM tbl_categories WHERE name = 'Phụ kiện'),
 (SELECT id FROM tbl_brands WHERE name = 'Logitech'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Logitech Brio 4K', 'Webcam Logitech Brio 4K, 4K 30fps, HDR, Windows Hello, streaming pro', 4990000, 20,
 (SELECT id FROM tbl_categories WHERE name = 'Phụ kiện'),
 (SELECT id FROM tbl_brands WHERE name = 'Logitech'),
 NULL,
 24, 'ACTIVE', NOW(), NOW());


-- =====================================================
-- 13. THIẾT BỊ MẠNG - Router, Access Point
-- =====================================================
INSERT INTO tbl_products (id, name, description, price, stock, category_id, brand_id, product_type_id, warranty_period, status, created_at, updated_at)
VALUES
-- ASUS Routers
(gen_random_uuid(), 'ASUS ROG Rapture GT-AX11000 Pro', 'Router gaming ASUS ROG GT-AX11000 Pro, WiFi 6, tri-band, 2.5G WAN, gaming port', 12990000, 10,
 (SELECT id FROM tbl_categories WHERE name = 'Thiết bị mạng'),
 (SELECT id FROM tbl_brands WHERE name = 'ASUS'),
 (SELECT id FROM tbl_product_types WHERE name = 'NETWORK_CARD'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'ASUS RT-AX86U Pro', 'Router ASUS RT-AX86U Pro, WiFi 6, dual-band, 2.5G port, AiMesh, gaming', 6990000, 18,
 (SELECT id FROM tbl_categories WHERE name = 'Thiết bị mạng'),
 (SELECT id FROM tbl_brands WHERE name = 'ASUS'),
 (SELECT id FROM tbl_product_types WHERE name = 'NETWORK_CARD'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'ASUS ZenWiFi Pro ET12 (2-pack)', 'Mesh WiFi ASUS ZenWiFi Pro ET12, WiFi 6E, tri-band, 2 pack, coverage 6000 sqft', 14990000, 12,
 (SELECT id FROM tbl_categories WHERE name = 'Thiết bị mạng'),
 (SELECT id FROM tbl_brands WHERE name = 'ASUS'),
 (SELECT id FROM tbl_product_types WHERE name = 'NETWORK_CARD'),
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'ASUS RT-AX55', 'Router ASUS RT-AX55, WiFi 6, dual-band, AiMesh, giá tốt cho gia đình', 1490000, 40,
 (SELECT id FROM tbl_categories WHERE name = 'Thiết bị mạng'),
 (SELECT id FROM tbl_brands WHERE name = 'ASUS'),
 (SELECT id FROM tbl_product_types WHERE name = 'NETWORK_CARD'),
 36, 'ACTIVE', NOW(), NOW());


-- =====================================================
-- 14. PC - Máy tính để bàn build sẵn
-- =====================================================
INSERT INTO tbl_products (id, name, description, price, stock, category_id, brand_id, product_type_id, warranty_period, status, created_at, updated_at)
VALUES
-- Gaming PCs
(gen_random_uuid(), 'PC Gaming Intel i9-14900K RTX 4090', 'PC Gaming cao cấp: Intel i9-14900K, RTX 4090 24GB, 64GB DDR5, 2TB NVMe, AIO 360mm, 1000W Gold', 89990000, 5,
 (SELECT id FROM tbl_categories WHERE name = 'PC'),
 (SELECT id FROM tbl_brands WHERE name = 'Intel'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'PC Gaming Intel i7-14700K RTX 4080 SUPER', 'PC Gaming: Intel i7-14700K, RTX 4080 SUPER 16GB, 32GB DDR5, 1TB NVMe, AIO 360mm, 850W Gold', 59990000, 8,
 (SELECT id FROM tbl_categories WHERE name = 'PC'),
 (SELECT id FROM tbl_brands WHERE name = 'Intel'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'PC Gaming AMD Ryzen 7 7800X3D RTX 4070 Ti SUPER', 'PC Gaming: AMD Ryzen 7 7800X3D, RTX 4070 Ti SUPER 16GB, 32GB DDR5, 1TB NVMe, AIO 240mm', 45990000, 10,
 (SELECT id FROM tbl_categories WHERE name = 'PC'),
 (SELECT id FROM tbl_brands WHERE name = 'AMD'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'PC Gaming Intel i5-14600K RTX 4070 SUPER', 'PC Gaming: Intel i5-14600K, RTX 4070 SUPER 12GB, 32GB DDR5, 1TB NVMe, Tower Cooler', 35990000, 12,
 (SELECT id FROM tbl_categories WHERE name = 'PC'),
 (SELECT id FROM tbl_brands WHERE name = 'Intel'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'PC Gaming AMD Ryzen 5 7600X RTX 4060 Ti', 'PC Gaming tầm trung: AMD Ryzen 5 7600X, RTX 4060 Ti 8GB, 16GB DDR5, 512GB NVMe', 25990000, 15,
 (SELECT id FROM tbl_categories WHERE name = 'PC'),
 (SELECT id FROM tbl_brands WHERE name = 'AMD'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'PC Gaming Intel i5-13400F RTX 4060', 'PC Gaming giá tốt: Intel i5-13400F, RTX 4060 8GB, 16GB DDR4, 512GB NVMe', 18990000, 20,
 (SELECT id FROM tbl_categories WHERE name = 'PC'),
 (SELECT id FROM tbl_brands WHERE name = 'Intel'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

-- Office PCs
(gen_random_uuid(), 'PC Văn phòng Intel i5-13400', 'PC Văn phòng: Intel i5-13400, Intel UHD 730, 16GB DDR4, 512GB NVMe, nhỏ gọn', 12990000, 25,
 (SELECT id FROM tbl_categories WHERE name = 'PC'),
 (SELECT id FROM tbl_brands WHERE name = 'Intel'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'PC Văn phòng AMD Ryzen 5 5600G', 'PC Văn phòng: AMD Ryzen 5 5600G, Radeon Graphics, 16GB DDR4, 512GB NVMe', 10990000, 30,
 (SELECT id FROM tbl_categories WHERE name = 'PC'),
 (SELECT id FROM tbl_brands WHERE name = 'AMD'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'PC Văn phòng Intel i3-13100', 'PC Văn phòng cơ bản: Intel i3-13100, Intel UHD 730, 8GB DDR4, 256GB NVMe', 7990000, 35,
 (SELECT id FROM tbl_categories WHERE name = 'PC'),
 (SELECT id FROM tbl_brands WHERE name = 'Intel'),
 NULL,
 24, 'ACTIVE', NOW(), NOW()),

-- Workstation PCs
(gen_random_uuid(), 'Workstation Intel i9-14900K RTX 4000 Ada', 'Workstation chuyên nghiệp: Intel i9-14900K, NVIDIA RTX 4000 Ada 20GB, 128GB DDR5 ECC, 2TB NVMe', 119990000, 3,
 (SELECT id FROM tbl_categories WHERE name = 'PC'),
 (SELECT id FROM tbl_brands WHERE name = 'Intel'),
 NULL,
 36, 'ACTIVE', NOW(), NOW()),

(gen_random_uuid(), 'Workstation AMD Ryzen 9 7950X RTX A4000', 'Workstation: AMD Ryzen 9 7950X, NVIDIA RTX A4000 16GB, 64GB DDR5, 2TB NVMe', 79990000, 4,
 (SELECT id FROM tbl_categories WHERE name = 'PC'),
 (SELECT id FROM tbl_brands WHERE name = 'AMD'),
 NULL,
 36, 'ACTIVE', NOW(), NOW());

-- =====================================================
-- HOÀN TẤT SEEDING
-- =====================================================
-- Tổng cộng: ~100 sản phẩm đa dạng cho cửa hàng máy tính
-- Bao gồm: CPU, Mainboard, RAM, GPU, PSU, Case, Storage, Cooler, 
--          Monitor, Laptop, Gaming Gear, Phụ kiện, Thiết bị mạng, PC build sẵn
