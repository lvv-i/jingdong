-- H2 兼容建库建表脚本（MODE=MySQL）
-- 来源：backend/sql/20260812_001_schema.sql（MySQL 原版）
-- 适配：去掉 ENGINE/ON UPDATE/TINYINT→SMALLINT/BIGINT UNSIGNED→BIGINT
-- 执行：由 Spring Boot spring.sql.init 自动执行

-- 1. users
CREATE TABLE `users` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT,
  `username`      VARCHAR(50) NOT NULL,
  `password_hash` VARCHAR(100) NOT NULL,
  `phone`         VARCHAR(20) NOT NULL,
  `role`          VARCHAR(20) NOT NULL DEFAULT 'USER',
  `status`        VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_flag`  SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`)
);
CREATE INDEX `idx_users_status` ON `users` (`status`);

-- 2. merchant_shops
CREATE TABLE `merchant_shops` (
  `id`           BIGINT NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT NOT NULL,
  `shop_name`    VARCHAR(100) NOT NULL,
  `category_id`  BIGINT NOT NULL,
  `description`  VARCHAR(500) DEFAULT NULL,
  `audit_status` VARCHAR(32) NOT NULL DEFAULT 'PENDING_AUDIT',
  `audit_reason` VARCHAR(500) DEFAULT NULL,
  `status`       VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
  `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_flag` SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_user_id` (`user_id`)
);
CREATE INDEX `idx_ms_category_id` ON `merchant_shops` (`category_id`);
CREATE INDEX `idx_ms_audit_status` ON `merchant_shops` (`audit_status`);

-- 3. categories
CREATE TABLE `categories` (
  `id`           BIGINT NOT NULL AUTO_INCREMENT,
  `parent_id`    BIGINT NOT NULL DEFAULT 0,
  `name`         VARCHAR(50) NOT NULL,
  `sort_order`   INT NOT NULL DEFAULT 0,
  `status`       VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_flag` SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
);
CREATE INDEX `idx_cat_parent_id` ON `categories` (`parent_id`);

-- 4. products
CREATE TABLE `products` (
  `id`             BIGINT NOT NULL AUTO_INCREMENT,
  `merchant_id`    BIGINT NOT NULL,
  `category_id`    BIGINT NOT NULL,
  `title`          VARCHAR(200) NOT NULL,
  `sub_title`      VARCHAR(200) DEFAULT NULL,
  `price`          DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `original_price` DECIMAL(12,2) DEFAULT NULL,
  `stock`          INT NOT NULL DEFAULT 0,
  `sales_count`    INT NOT NULL DEFAULT 0,
  `main_image`     VARCHAR(500) NOT NULL,
  `detail`         TEXT,
  `status`         VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_flag`   SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
);
CREATE INDEX `idx_p_merchant_id` ON `products` (`merchant_id`);
CREATE INDEX `idx_p_category_id` ON `products` (`category_id`);
CREATE INDEX `idx_p_status` ON `products` (`status`);
CREATE INDEX `idx_p_created_at` ON `products` (`created_at`);

-- 5. product_images
CREATE TABLE `product_images` (
  `id`           BIGINT NOT NULL AUTO_INCREMENT,
  `product_id`   BIGINT NOT NULL,
  `image_url`    VARCHAR(500) NOT NULL,
  `sort_order`   INT NOT NULL DEFAULT 0,
  `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_flag` SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
);
CREATE INDEX `idx_pi_product_id` ON `product_images` (`product_id`);

-- 6. cart_items
CREATE TABLE `cart_items` (
  `id`           BIGINT NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT NOT NULL,
  `product_id`   BIGINT NOT NULL,
  `quantity`     INT NOT NULL DEFAULT 1,
  `selected`     SMALLINT NOT NULL DEFAULT 1,
  `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_flag` SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
);
CREATE INDEX `idx_ci_user_id` ON `cart_items` (`user_id`);
CREATE INDEX `idx_ci_product_id` ON `cart_items` (`product_id`);

-- 7. orders
CREATE TABLE `orders` (
  `id`                BIGINT NOT NULL AUTO_INCREMENT,
  `order_no`          VARCHAR(32) NOT NULL,
  `user_id`           BIGINT NOT NULL,
  `merchant_id`       BIGINT NOT NULL,
  `total_amount`      DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `pay_amount`        DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `receiver_snapshot` VARCHAR(500) NOT NULL,
  `shipping_no`       VARCHAR(50) DEFAULT NULL,
  `paid_at`           DATETIME DEFAULT NULL,
  `shipped_at`        DATETIME DEFAULT NULL,
  `completed_at`      DATETIME DEFAULT NULL,
  `cancelled_at`      DATETIME DEFAULT NULL,
  `status`            VARCHAR(32) NOT NULL DEFAULT 'PENDING_PAY',
  `created_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_flag`      SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
);
CREATE INDEX `idx_o_user_id` ON `orders` (`user_id`);
CREATE INDEX `idx_o_merchant_id` ON `orders` (`merchant_id`);
CREATE INDEX `idx_o_status` ON `orders` (`status`);
CREATE INDEX `idx_o_created_at` ON `orders` (`created_at`);
CREATE INDEX `idx_o_user_status` ON `orders` (`user_id`, `status`);
CREATE INDEX `idx_o_merchant_status` ON `orders` (`merchant_id`, `status`);

-- 8. order_items
CREATE TABLE `order_items` (
  `id`             BIGINT NOT NULL AUTO_INCREMENT,
  `order_id`       BIGINT NOT NULL,
  `product_id`     BIGINT NOT NULL,
  `title_snapshot` VARCHAR(200) NOT NULL,
  `price_snapshot` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `quantity`       INT NOT NULL DEFAULT 1,
  `total_price`    DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `rating`         SMALLINT DEFAULT NULL,
  `comment`        VARCHAR(500) DEFAULT NULL,
  `reviewed_at`    DATETIME DEFAULT NULL,
  `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_flag`   SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
);
CREATE INDEX `idx_oi_order_id` ON `order_items` (`order_id`);
CREATE INDEX `idx_oi_product_id` ON `order_items` (`product_id`);

-- 9. refund_requests
CREATE TABLE `refund_requests` (
  `id`             BIGINT NOT NULL AUTO_INCREMENT,
  `refund_no`      VARCHAR(32) NOT NULL,
  `order_id`       BIGINT NOT NULL,
  `user_id`        BIGINT NOT NULL,
  `merchant_id`    BIGINT NOT NULL,
  `refund_amount`  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `reason`         VARCHAR(500) NOT NULL,
  `merchant_reply` VARCHAR(500) DEFAULT NULL,
  `admin_result`   VARCHAR(500) DEFAULT NULL,
  `status`         VARCHAR(32) NOT NULL DEFAULT 'REFUNDING',
  `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_flag`   SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`)
);
CREATE INDEX `idx_rf_order_id` ON `refund_requests` (`order_id`);
CREATE INDEX `idx_rf_user_id` ON `refund_requests` (`user_id`);
CREATE INDEX `idx_rf_merchant_id` ON `refund_requests` (`merchant_id`);
CREATE INDEX `idx_rf_status` ON `refund_requests` (`status`);
CREATE INDEX `idx_rf_created_at` ON `refund_requests` (`created_at`);

-- 10. payment_records
CREATE TABLE `payment_records` (
  `id`           BIGINT NOT NULL AUTO_INCREMENT,
  `payment_no`   VARCHAR(32) NOT NULL,
  `order_id`     BIGINT NOT NULL,
  `user_id`      BIGINT NOT NULL,
  `refund_id`    BIGINT DEFAULT NULL,
  `amount`       DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `type`         VARCHAR(20) NOT NULL DEFAULT 'PAY',
  `status`       VARCHAR(32) NOT NULL DEFAULT 'SUCCESS',
  `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_flag` SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`)
);
CREATE INDEX `idx_pr_order_id` ON `payment_records` (`order_id`);
CREATE INDEX `idx_pr_user_id` ON `payment_records` (`user_id`);

-- 11. addresses
CREATE TABLE `addresses` (
  `id`           BIGINT NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT NOT NULL,
  `receiver`     VARCHAR(50) NOT NULL,
  `phone`        VARCHAR(20) NOT NULL,
  `province`     VARCHAR(50) NOT NULL,
  `city`         VARCHAR(50) NOT NULL,
  `district`     VARCHAR(50) DEFAULT NULL,
  `detail`       VARCHAR(200) NOT NULL,
  `is_default`   SMALLINT NOT NULL DEFAULT 0,
  `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_flag` SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
);
CREATE INDEX `idx_addr_user_id` ON `addresses` (`user_id`);

-- 12. notices
CREATE TABLE `notices` (
  `id`           BIGINT NOT NULL AUTO_INCREMENT,
  `receiver_id`  BIGINT NOT NULL,
  `title`        VARCHAR(200) NOT NULL,
  `content`      TEXT NOT NULL,
  `read_status`  SMALLINT NOT NULL DEFAULT 0,
  `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_flag` SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
);
CREATE INDEX `idx_notice_receiver_id` ON `notices` (`receiver_id`);

-- 13. audit_logs
CREATE TABLE `audit_logs` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT,
  `operator_id`   BIGINT NOT NULL,
  `operator_role` VARCHAR(20) NOT NULL,
  `target_type`   VARCHAR(50) NOT NULL,
  `target_id`     BIGINT NOT NULL,
  `action`        VARCHAR(50) NOT NULL,
  `remark`        VARCHAR(500) DEFAULT NULL,
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_flag`  SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
);
CREATE INDEX `idx_al_operator_id` ON `audit_logs` (`operator_id`);
CREATE INDEX `idx_al_target_id` ON `audit_logs` (`target_id`);
CREATE INDEX `idx_al_created_at` ON `audit_logs` (`created_at`);