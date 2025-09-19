CREATE TABLE "tbl_users" (
  id BIGSERIAL PRIMARY KEY,
  username varchar UNIQUE NOT NULL,
  password varchar NOT NULL,
  first_name varchar,
  last_name varchar,
  gender int,
  date_of_birth date,
  phone varchar(15),
  email varchar UNIQUE,
  created_at timestamp,
  updated_at timestamp
);

CREATE TABLE "tbl_role" (
  id SERIAL PRIMARY KEY,
  name varchar NOT NULL,
  description varchar,
  created_at timestamp,
  updated_at timestamp
);

CREATE TABLE "tbl_userrole" (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT,
  role_id INT,
  created_at timestamp,
  updated_at timestamp
);

CREATE TABLE "tbl_token" (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT,
  access_token text,
  refresh_token text,
  created_at timestamp,
  updated_at timestamp
);

CREATE TABLE "tbl_address" (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT,
  apartment_number varchar,
  street_number varchar,
  ward varchar,
  city varchar,
  created_at timestamp,
  updated_at timestamp
);

CREATE TABLE "tbl_employees" (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT,
  position varchar,
  created_at timestamp,
  updated_at timestamp
);

CREATE TABLE "tbl_categories" (
  id SERIAL PRIMARY KEY,
  name varchar NOT NULL,
  description varchar,
  created_at timestamp,
  updated_at timestamp
);

CREATE TABLE "tbl_brands" (
  id SERIAL PRIMARY KEY,
  name varchar NOT NULL,
  country varchar,
  description varchar,
  image_url varchar,
  created_at timestamp,
  updated_at timestamp
);

CREATE TABLE "tbl_products" (
  id BIGSERIAL PRIMARY KEY,
  name varchar NOT NULL,
  description text,
  category_id INT,
  brand_id INT,
  warranty_period int,
  image_url varchar,
  created_at timestamp,
  updated_at timestamp
);

CREATE TABLE "tbl_product_type" (
  id BIGSERIAL PRIMARY KEY,
  product_id BIGINT,
  sku varchar UNIQUE,
  price decimal(12,2),
  stock int,
  image_url varchar,
  created_at timestamp,
  updated_at timestamp
);

CREATE TABLE "tbl_product_type_specs" (
  id BIGSERIAL PRIMARY KEY,
  type_id BIGINT,
  spec_key varchar NOT NULL,
  spec_value varchar NOT NULL,
  created_at timestamp,
  updated_at timestamp
);

CREATE TABLE "tbl_orders" (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT,
  order_date timestamp,
  total_amount decimal(12,2),
  status varchar,
  payment_method varchar,
  payment_status varchar,
  created_at timestamp,
  updated_at timestamp
);

CREATE TABLE "tbl_order_details" (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT,
  variant_id BIGINT,
  quantity int,
  unit_price decimal(12,2),
  subtotal decimal(12,2),
  created_at timestamp,
  updated_at timestamp
);

CREATE TABLE "tbl_payments" (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT,
  transaction_id varchar,
  amount decimal(12,2),
  payment_date timestamp,
  payment_status varchar,
  created_at timestamp,
  updated_at timestamp
);

CREATE TABLE "tbl_promotions" (
  id SERIAL PRIMARY KEY,
  promo_code varchar UNIQUE,
  description varchar,
  discount_percent decimal(5,2),
  start_date timestamp,
  end_date timestamp,
  is_active boolean,
  created_at timestamp,
  updated_at timestamp
);

CREATE TABLE "tbl_order_promotions" (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT,
  promo_id INT,
  discount_amount decimal(12,2),
  created_at timestamp
);

CREATE TABLE "tbl_warranty" (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT,
  variant_id BIGINT,
  user_id BIGINT,
  request_date timestamp,
  status varchar,
  description text,
  created_at timestamp,
  updated_at timestamp
);

CREATE TABLE "tbl_chatlogs" (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT,
  message text,
  response text,
  timestamp timestamp
);

-- =============================
-- Foreign Keys
-- =============================
ALTER TABLE "tbl_userrole" ADD FOREIGN KEY (user_id) REFERENCES "tbl_users" (id);
ALTER TABLE "tbl_userrole" ADD FOREIGN KEY (role_id) REFERENCES "tbl_role" (id);

ALTER TABLE "tbl_token" ADD FOREIGN KEY (user_id) REFERENCES "tbl_users" (id);

ALTER TABLE "tbl_address" ADD FOREIGN KEY (user_id) REFERENCES "tbl_users" (id);

ALTER TABLE "tbl_employees" ADD FOREIGN KEY (user_id) REFERENCES "tbl_users" (id);

ALTER TABLE "tbl_products" ADD FOREIGN KEY (category_id) REFERENCES "tbl_categories" (id);
ALTER TABLE "tbl_products" ADD FOREIGN KEY (brand_id) REFERENCES "tbl_brands" (id);

ALTER TABLE "tbl_product_type" ADD FOREIGN KEY (product_id) REFERENCES "tbl_products" (id);

ALTER TABLE "tbl_product_type_specs" ADD FOREIGN KEY (type_id) REFERENCES "tbl_product_type" (id);

ALTER TABLE "tbl_orders" ADD FOREIGN KEY (user_id) REFERENCES "tbl_users" (id);

ALTER TABLE "tbl_order_details" ADD FOREIGN KEY (order_id) REFERENCES "tbl_orders" (id);
ALTER TABLE "tbl_order_details" ADD FOREIGN KEY (variant_id) REFERENCES "tbl_product_type" (id);

ALTER TABLE "tbl_payments" ADD FOREIGN KEY (order_id) REFERENCES "tbl_orders" (id);
s
ALTER TABLE "tbl_order_promotions" ADD FOREIGN KEY (order_id) REFERENCES "tbl_orders" (id);
ALTER TABLE "tbl_order_promotions" ADD FOREIGN KEY (promo_id) REFERENCES "tbl_promotions" (id);

ALTER TABLE "tbl_warranty" ADD FOREIGN KEY (order_id) REFERENCES "tbl_orders" (id);
ALTER TABLE "tbl_warranty" ADD FOREIGN KEY (variant_id) REFERENCES "tbl_product_type" (id);
ALTER TABLE "tbl_warranty" ADD FOREIGN KEY (user_id) REFERENCES "tbl_users" (id);

ALTER TABLE "tbl_chatlogs" ADD FOREIGN KEY (user_id) REFERENCES "tbl_users" (id);

ALTER TABLE tbl_users ADD COLUMN status VARCHAR(20) DEFAULT 'active';

ALTER TABLE tbl_users ADD COLUMN type VARCHAR(20) DEFAULT 'customer';

ALTER TABLE tbl_address
ADD COLUMN address_type INT DEFAULT 1;

select * from tbl_users
select * from tbl_address






