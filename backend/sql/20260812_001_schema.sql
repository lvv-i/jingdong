-- =============================================================
-- 京东风格电商平台 建库建表脚本
-- 文件：20260812_001_schema.sql
-- 依据：docs/phase1/member-a/deliverables/02-数据字典v1.0.md（13 张表）
-- 规范：.qoder/rules/db-conventions.md
-- 执行：mysql -u root -p < 20260812_001_schema.sql
-- 回滚：见文件末尾注释（DROP TABLE 语句，按依赖逆序）
-- =============================================================

CREATE DATABASE IF NOT EXISTS `jd_shop` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `jd_shop`;

-- -------------------------------------------------------------
-- 1. users 用户表
-- -------------------------------------------------------------
CREATE TABLE `users` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`      VARCHAR(50)     NOT NULL COMMENT '用户名（登录名）',
  `password_hash` VARCHAR(100)    NOT NULL COMMENT '密码哈希（BCrypt）',
  `phone`         VARCHAR(20)     NOT NULL COMMENT '手机号',
  `role`          VARCHAR(20)     NOT NULL DEFAULT 'USER' COMMENT '角色：USER普通用户 MERCHANT商家 ADMIN管理员',
  `status`        VARCHAR(32)     NOT NULL DEFAULT 'NORMAL' COMMENT '状态：NORMAL正常 DISABLED禁用',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag`  TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -------------------------------------------------------------
-- 2. merchant_shops 商家店铺表
-- -------------------------------------------------------------
CREATE TABLE `merchant_shops` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '店铺ID',
  `user_id`      BIGINT UNSIGNED NOT NULL COMMENT '商家用户ID（一个用户一个店铺）',
  `shop_name`    VARCHAR(100)    NOT NULL COMMENT '店铺名称',
  `category_id`  BIGINT UNSIGNED NOT NULL COMMENT '主营类目ID',
  `description`  VARCHAR(500)    DEFAULT NULL COMMENT '店铺简介',
  `audit_status` VARCHAR(32)     NOT NULL DEFAULT 'PENDING_AUDIT' COMMENT '入驻审核状态：PENDING_AUDIT待审核 APPROVED已通过 REJECTED已驳回',
  `audit_reason` VARCHAR(500)    DEFAULT NULL COMMENT '管理员审核意见/驳回原因（留痕）',
  `status`       VARCHAR(32)     NOT NULL DEFAULT 'NORMAL' COMMENT '店铺营业状态：NORMAL正常 CLOSED关店',
  `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家店铺表';

-- -------------------------------------------------------------
-- 3. categories 商品类目表
-- -------------------------------------------------------------
CREATE TABLE `categories` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '类目ID',
  `parent_id`    BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父类目ID（0为顶级类目）',
  `name`         VARCHAR(50)     NOT NULL COMMENT '类目名称',
  `sort_order`   INT             NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
  `status`       VARCHAR(32)     NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED启用 DISABLED禁用',
  `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品类目表';

-- -------------------------------------------------------------
-- 4. products 商品主表
-- -------------------------------------------------------------
CREATE TABLE `products` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `merchant_id`    BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
  `category_id`    BIGINT UNSIGNED NOT NULL COMMENT '类目ID',
  `title`          VARCHAR(200)    NOT NULL COMMENT '商品标题',
  `sub_title`      VARCHAR(200)    DEFAULT NULL COMMENT '商品副标题',
  `price`          DECIMAL(12,2)   NOT NULL DEFAULT 0.00 COMMENT '售价',
  `original_price` DECIMAL(12,2)   DEFAULT NULL COMMENT '原价（划线价，可为空）',
  `stock`          INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '库存数量',
  `sales_count`    INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '累计销量',
  `main_image`     VARCHAR(500)    NOT NULL COMMENT '主图URL',
  `detail`         TEXT            COMMENT '商品详情（富文本）',
  `status`         VARCHAR(32)     NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT草稿 PENDING_ON_SALE待上架 ON_SALE已上架 OFF_SALE已下架',
  `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag`   TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品主表';

-- -------------------------------------------------------------
-- 5. product_images 商品图片表（扩展表 1，纯链接表省略 status）
-- -------------------------------------------------------------
CREATE TABLE `product_images` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `product_id`   BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `image_url`    VARCHAR(500)    NOT NULL COMMENT '图片URL',
  `sort_order`   INT             NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
  `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- -------------------------------------------------------------
-- 6. cart_items 购物车表（纯链接表省略 status）
-- -------------------------------------------------------------
CREATE TABLE `cart_items` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '购物车条目ID',
  `user_id`      BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `product_id`   BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `quantity`     INT UNSIGNED    NOT NULL DEFAULT 1 COMMENT '数量',
  `selected`     TINYINT         NOT NULL DEFAULT 1 COMMENT '是否勾选结算：0未选 1已选',
  `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- -------------------------------------------------------------
-- 7. orders 订单主表
-- -------------------------------------------------------------
CREATE TABLE `orders` (
  `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no`          VARCHAR(32)     NOT NULL COMMENT '订单编号',
  `user_id`           BIGINT UNSIGNED NOT NULL COMMENT '下单用户ID',
  `merchant_id`       BIGINT UNSIGNED NOT NULL COMMENT '商家ID（一单一商家，跨店拆单）',
  `total_amount`      DECIMAL(12,2)   NOT NULL DEFAULT 0.00 COMMENT '商品总金额',
  `pay_amount`        DECIMAL(12,2)   NOT NULL DEFAULT 0.00 COMMENT '实付金额',
  `receiver_snapshot` VARCHAR(500)    NOT NULL COMMENT '收货信息快照（收件人+电话+完整地址，下单时冻结）',
  `shipping_no`       VARCHAR(50)     DEFAULT NULL COMMENT '运单号（发货时必填）',
  `paid_at`           DATETIME        DEFAULT NULL COMMENT '支付时间',
  `shipped_at`        DATETIME        DEFAULT NULL COMMENT '发货时间',
  `completed_at`      DATETIME        DEFAULT NULL COMMENT '完成时间',
  `cancelled_at`      DATETIME        DEFAULT NULL COMMENT '取消时间',
  `status`            VARCHAR(32)     NOT NULL DEFAULT 'PENDING_PAY' COMMENT '状态：PENDING_PAY待支付 PAID已支付待发货 SHIPPED已发货待收货 COMPLETED已完成 CANCELLED已取消',
  `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag`      TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_merchant_status` (`merchant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- -------------------------------------------------------------
-- 8. order_items 订单明细表（纯链接表省略 status；含评价扩展字段）
-- -------------------------------------------------------------
CREATE TABLE `order_items` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id`       BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `product_id`     BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `title_snapshot` VARCHAR(200)    NOT NULL COMMENT '商品标题快照（下单时冻结）',
  `price_snapshot` DECIMAL(12,2)   NOT NULL DEFAULT 0.00 COMMENT '成交单价快照（下单时冻结）',
  `quantity`       INT UNSIGNED    NOT NULL DEFAULT 1 COMMENT '购买数量',
  `total_price`    DECIMAL(12,2)   NOT NULL DEFAULT 0.00 COMMENT '小计金额（price_snapshot × quantity）',
  `rating`         TINYINT         DEFAULT NULL COMMENT '评价评分 1-5（NULL=未评价；仅订单 COMPLETED 后可写）',
  `comment`        VARCHAR(500)    DEFAULT NULL COMMENT '评价内容（≤200字；rating 写入时必填）',
  `reviewed_at`    DATETIME        DEFAULT NULL COMMENT '评价时间',
  `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag`   TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- -------------------------------------------------------------
-- 9. refund_requests 退款申请表
-- -------------------------------------------------------------
CREATE TABLE `refund_requests` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '退款单ID',
  `refund_no`      VARCHAR(32)     NOT NULL COMMENT '退款单编号',
  `order_id`       BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `user_id`        BIGINT UNSIGNED NOT NULL COMMENT '申请用户ID',
  `merchant_id`    BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
  `refund_amount`  DECIMAL(12,2)   NOT NULL DEFAULT 0.00 COMMENT '退款金额（≤订单实付金额，后端校验）',
  `reason`         VARCHAR(500)    NOT NULL COMMENT '退款原因（用户填写）',
  `merchant_reply` VARCHAR(500)    DEFAULT NULL COMMENT '商家回复（同意/拒绝意见，留痕）',
  `admin_result`   VARCHAR(500)    DEFAULT NULL COMMENT '管理员裁决结果（平台介入，留痕）',
  `status`         VARCHAR(32)     NOT NULL DEFAULT 'REFUNDING' COMMENT '状态：REFUNDING退款中 MERCHANT_AGREED商家同意 MERCHANT_REJECTED商家拒绝 ADMIN_INTERVENED平台介入 REFUNDED已退款 CLOSED已关闭',
  `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag`   TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款申请表';

-- -------------------------------------------------------------
-- 10. payment_records 支付流水表（扩展表 2）
-- -------------------------------------------------------------
CREATE TABLE `payment_records` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `payment_no`   VARCHAR(32)     NOT NULL COMMENT '支付流水号',
  `order_id`     BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `user_id`      BIGINT UNSIGNED NOT NULL COMMENT '支付用户ID',
  `refund_id`    BIGINT UNSIGNED DEFAULT NULL COMMENT '关联退款单ID（退款流水时填写）',
  `amount`       DECIMAL(12,2)   NOT NULL DEFAULT 0.00 COMMENT '金额',
  `type`         VARCHAR(20)     NOT NULL DEFAULT 'PAY' COMMENT '流水类型：PAY支付 REFUND退款',
  `status`       VARCHAR(32)     NOT NULL DEFAULT 'SUCCESS' COMMENT '状态：SUCCESS成功 FAILED失败',
  `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付流水表';

-- -------------------------------------------------------------
-- 11. addresses 收货地址表（资料表省略 status）
-- -------------------------------------------------------------
CREATE TABLE `addresses` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id`      BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `receiver`     VARCHAR(50)     NOT NULL COMMENT '收货人姓名',
  `phone`        VARCHAR(20)     NOT NULL COMMENT '联系电话',
  `province`     VARCHAR(50)     NOT NULL COMMENT '省',
  `city`         VARCHAR(50)     NOT NULL COMMENT '市',
  `district`     VARCHAR(50)     DEFAULT NULL COMMENT '区/县',
  `detail`       VARCHAR(200)    NOT NULL COMMENT '详细地址',
  `is_default`   TINYINT         NOT NULL DEFAULT 0 COMMENT '是否默认地址：0否 1是',
  `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- -------------------------------------------------------------
-- 12. notices 站内通知表（read_status 表达业务状态，省略 status）
-- -------------------------------------------------------------
CREATE TABLE `notices` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `receiver_id`  BIGINT UNSIGNED NOT NULL COMMENT '接收者用户ID',
  `title`        VARCHAR(200)    NOT NULL COMMENT '通知标题',
  `content`      TEXT            NOT NULL COMMENT '通知内容',
  `read_status`  TINYINT         NOT NULL DEFAULT 0 COMMENT '已读状态：0未读 1已读',
  `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_receiver_id` (`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内通知表';

-- -------------------------------------------------------------
-- 13. audit_logs 审计日志表（日志表省略 status）
-- -------------------------------------------------------------
CREATE TABLE `audit_logs` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `operator_id`   BIGINT UNSIGNED NOT NULL COMMENT '操作者用户ID',
  `operator_role` VARCHAR(20)     NOT NULL COMMENT '操作者角色：USER MERCHANT ADMIN',
  `target_type`   VARCHAR(50)     NOT NULL COMMENT '目标类型：ORDER订单 REFUND退款 MERCHANT商家 PRODUCT商品',
  `target_id`     BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
  `action`        VARCHAR(50)     NOT NULL COMMENT '动作：SHIP发货 APPROVE通过 REJECT驳回 HANDLE_REFUND退款裁决 TAKE_DOWN强制下架',
  `remark`        VARCHAR(500)    DEFAULT NULL COMMENT '备注（审核意见/裁决结果等）',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag`  TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_target_id` (`target_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- =============================================================
-- 回滚脚本（按依赖逆序执行）
-- USE jd_shop;
-- DROP TABLE IF EXISTS audit_logs, notices, addresses, payment_records,
--   refund_requests, order_items, orders, cart_items, product_images,
--   products, categories, merchant_shops, users;
-- =============================================================
