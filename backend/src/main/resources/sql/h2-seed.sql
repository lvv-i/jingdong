-- H2 兼容种子数据（MODE=MySQL）
-- 来源：backend/sql/20260812_002_seed.sql（MySQL 原版）
-- 说明：password_hash 为占位值，DataInitializer 启动时自动重置为 BCrypt(username)

-- 1. 固定测试账号（密码=账号名，DataInitializer 保证哈希正确）
INSERT INTO `users` (`id`, `username`, `password_hash`, `phone`, `role`, `status`) VALUES
(1, 'user001',     '$2a$10$placeholder00000000000000000000000000000000000000000000000000', '13800000001', 'USER',     'NORMAL'),
(2, 'user002',     '$2a$10$placeholder00000000000000000000000000000000000000000000000000', '13800000002', 'USER',     'NORMAL'),
(3, 'merchant001', '$2a$10$placeholder00000000000000000000000000000000000000000000000000', '13800000003', 'MERCHANT', 'NORMAL'),
(4, 'merchant002', '$2a$10$placeholder00000000000000000000000000000000000000000000000000', '13800000004', 'MERCHANT', 'NORMAL'),
(5, 'admin001',    '$2a$10$placeholder00000000000000000000000000000000000000000000000000', '13800000005', 'ADMIN',    'NORMAL');

-- 2. 店铺
INSERT INTO `merchant_shops` (`id`, `user_id`, `shop_name`, `category_id`, `description`, `audit_status`, `audit_reason`) VALUES
(1, 3, '京东自营数码旗舰店', 1, '数码家电官方演示店铺', 'APPROVED',      '资质齐全，审核通过'),
(2, 4, '待审核演示店铺',     1, '用于演示入驻审核流程', 'PENDING_AUDIT', NULL);

-- 3. 类目
INSERT INTO `categories` (`id`, `parent_id`, `name`, `sort_order`, `status`) VALUES
(1,  0, '数码家电', 1, 'ENABLED'),
(2,  0, '服饰鞋包', 2, 'ENABLED'),
(3,  0, '食品生鲜', 3, 'ENABLED'),
(4,  0, '家居日用', 4, 'ENABLED'),
(11, 1, '手机通讯', 1, 'ENABLED'),
(12, 1, '电脑办公', 2, 'ENABLED'),
(13, 1, '影音娱乐', 3, 'ENABLED'),
(21, 2, '男装',     1, 'ENABLED'),
(22, 2, '女装',     2, 'ENABLED'),
(31, 3, '休闲零食', 1, 'ENABLED'),
(32, 3, '水果蔬菜', 2, 'ENABLED'),
(41, 4, '厨房用品', 1, 'ENABLED'),
(42, 4, '清洁纸品', 2, 'ENABLED');

-- 4. 商品
INSERT INTO `products` (`id`, `merchant_id`, `category_id`, `title`, `sub_title`, `price`, `original_price`, `stock`, `sales_count`, `main_image`, `detail`, `status`) VALUES
(101, 1, 11, '京造 X1 智能手机 8GB+256GB',       '骁龙8系芯片 5000mAh 长续航',   1999.00, 2499.00, 100, 328,  'https://placehold.co/300x300/E27474/white?text=X1', '<p>京造 X1 智能手机详情</p>',       'ON_SALE'),
(102, 1, 11, '京造 X1 Pro 智能手机 12GB+512GB',  '旗舰影像 100W 快充',            3299.00, 3999.00, 50,  156,  'https://placehold.co/300x300/C81623/white?text=X1Pro', '<p>京造 X1 Pro 详情</p>',           'ON_SALE'),
(103, 1, 12, '轻薄笔记本电脑 14英寸',            '2.8K 屏 1.2kg 金属机身',        4599.00, 5299.00, 30,  89,   'https://placehold.co/300x300/409EFF/white?text=Laptop', '<p>轻薄本详情</p>',                 'ON_SALE'),
(104, 1, 13, '无线降噪耳机 Pro',                 '40dB 深度降噪 30 小时续航',      499.00,  699.00,  200, 512,  'https://placehold.co/300x300/2C3E50/white?text=Headphone', '<p>耳机详情</p>',                   'ON_SALE'),
(105, 1, 21, '纯棉商务休闲衬衫',                 '免烫抗皱 四季款',               129.00,  199.00,  300, 1203, 'https://placehold.co/300x300/27AE60/white?text=Shirt', '<p>衬衫详情</p>',                   'ON_SALE'),
(106, 1, 22, '法式连衣裙 收腰显瘦',              '春夏新款 雪纺面料',             239.00,  329.00,  150, 678,  'https://placehold.co/300x300/E91E63/white?text=Dress', '<p>连衣裙详情</p>',                 'ON_SALE'),
(107, 1, 31, '每日坚果礼盒 30包',                '混合坚果 独立小包',             89.90,   129.00,  500, 2230, 'https://placehold.co/300x300/D4AC0D/white?text=Nuts', '<p>坚果详情</p>',                   'ON_SALE'),
(108, 1, 32, '红富士苹果 5kg 装',                '脆甜多汁 产地直发',             49.90,   69.90,   400, 1890, 'https://placehold.co/300x300/E67E22/white?text=Apple', '<p>苹果详情</p>',                   'ON_SALE'),
(109, 1, 41, '不粘炒锅 32cm',                    '少油烟 电磁炉通用',             159.00,  259.00,  200, 856,  'https://placehold.co/300x300/7F8C8D/white?text=Pan', '<p>炒锅详情</p>',                   'ON_SALE'),
(110, 1, 42, '抽纸整箱 24包',                    '原生木浆 柔韧亲肤',             39.90,   59.90,   800, 3350, 'https://placehold.co/300x300/BDC3C7/black?text=Tissue', '<p>抽纸详情</p>',                   'ON_SALE'),
(111, 1, 11, '待上架演示商品',                    '审核流程演示',                  99.00,   NULL,    10,  0,    'https://placehold.co/300x300/95A5A6/white?text=Pending', NULL,                               'PENDING_ON_SALE'),
(112, 1, 11, '草稿演示商品',                      '商家草稿箱',                    199.00,  NULL,    10,  0,    'https://placehold.co/300x300/95A5A6/white?text=Draft', NULL,                               'DRAFT'),
(113, 1, 11, '已下架演示商品',                    '下架状态演示',                  299.00,  NULL,    10,  45,   'https://placehold.co/300x300/95A5A6/white?text=OffShelf', NULL,                               'OFF_SALE');

-- 商品多图
INSERT INTO `product_images` (`product_id`, `image_url`, `sort_order`) VALUES
(101, 'https://placehold.co/300x300/E27474/white?text=X1-Front', 1),
(101, 'https://placehold.co/300x300/E27474/white?text=X1-Back', 2),
(102, 'https://placehold.co/300x300/C81623/white?text=X1Pro-Front', 1),
(104, 'https://placehold.co/300x300/2C3E50/white?text=HP-Front', 1);

-- 5. 收货地址
INSERT INTO `addresses` (`id`, `user_id`, `receiver`, `phone`, `province`, `city`, `district`, `detail`, `is_default`) VALUES
(1, 1, '张演示', '13800000001', '北京市', '北京市', '海淀区', '中关村大街 1 号演示大厦 8 层', 1);

-- 6. 购物车
INSERT INTO `cart_items` (`id`, `user_id`, `product_id`, `quantity`, `selected`) VALUES
(1, 1, 101, 1, 1),
(2, 1, 107, 2, 1);

-- 7. 订单
INSERT INTO `orders` (`id`, `order_no`, `user_id`, `merchant_id`, `total_amount`, `pay_amount`, `receiver_snapshot`, `shipping_no`, `paid_at`, `shipped_at`, `completed_at`, `cancelled_at`, `status`) VALUES
(1, 'JD2026081200000001', 1, 1, 1999.00, 1999.00, '张演示,13800000001,北京市北京市海淀区中关村大街1号演示大厦8层', NULL,             NULL,                NULL,                NULL,                NULL,                'PENDING_PAY'),
(2, 'JD2026081200000002', 1, 1, 179.80,  179.80,  '张演示,13800000001,北京市北京市海淀区中关村大街1号演示大厦8层', NULL,             '2026-08-12 09:30:00', NULL,                NULL,                NULL,                'PAID'),
(3, 'JD2026081200000003', 1, 1, 499.00,  499.00,  '张演示,13800000001,北京市北京市海淀区中关村大街1号演示大厦8层', 'SF1234567890',   '2026-08-11 10:00:00', '2026-08-11 15:00:00', NULL,                NULL,                'SHIPPED'),
(4, 'JD2026081200000004', 1, 1, 129.00,  129.00,  '张演示,13800000001,北京市北京市海淀区中关村大街1号演示大厦8层', 'YT0987654321',   '2026-08-10 09:00:00', '2026-08-10 14:00:00', '2026-08-11 18:00:00', NULL,                'COMPLETED'),
(5, 'JD2026081200000005', 1, 1, 159.00,  159.00,  '张演示,13800000001,北京市北京市海淀区中关村大街1号演示大厦8层', NULL,             NULL,                NULL,                NULL,                '2026-08-12 08:00:00', 'CANCELLED');

-- 订单明细
INSERT INTO `order_items` (`id`, `order_id`, `product_id`, `title_snapshot`, `price_snapshot`, `quantity`, `total_price`, `rating`, `comment`, `reviewed_at`) VALUES
(1, 1, 101, '京造 X1 智能手机 8GB+256GB', 1999.00, 1, 1999.00, NULL, NULL, NULL),
(2, 2, 107, '每日坚果礼盒 30包',          89.90,   2, 179.80,  NULL, NULL, NULL),
(3, 3, 104, '无线降噪耳机 Pro',           499.00,  1, 499.00,  NULL, NULL, NULL),
(4, 4, 105, '纯棉商务休闲衬衫',           129.00,  1, 129.00,  5,    '面料舒服，做工好，推荐购买！', '2026-08-11 19:00:00'),
(5, 5, 109, '不粘炒锅 32cm',              159.00,  1, 159.00,  NULL, NULL, NULL);

-- 8. 支付流水
INSERT INTO `payment_records` (`id`, `payment_no`, `order_id`, `user_id`, `refund_id`, `amount`, `type`, `status`) VALUES
(1, 'PAY2026081200000001', 2, 1, NULL, 179.80, 'PAY', 'SUCCESS'),
(2, 'PAY2026081100000002', 3, 1, NULL, 499.00, 'PAY', 'SUCCESS'),
(3, 'PAY2026081000000003', 4, 1, NULL, 129.00, 'PAY', 'SUCCESS');

-- 9. 售后
INSERT INTO `refund_requests` (`id`, `refund_no`, `order_id`, `user_id`, `merchant_id`, `refund_amount`, `reason`, `merchant_reply`, `admin_result`, `status`) VALUES
(1, 'RF2026081200000001', 3, 1, 1, 499.00, '耳机有电流声，音质不满意，申请退货退款', NULL, NULL, 'REFUNDING'),
(2, 'RF2026081000000002', 4, 1, 1, 129.00, '衬衫尺码偏小，商家拒绝退款，申请平台介入', '商品已拆封影响二次销售，拒绝退款', NULL, 'ADMIN_INTERVENED');

-- 10. 站内通知
INSERT INTO `notices` (`id`, `receiver_id`, `title`, `content`, `read_status`) VALUES
(1, 1, '支付成功', '您的订单 JD2026081200000002 已支付成功，等待商家发货', 0),
(2, 1, '订单发货', '您的订单 JD2026081200000003 已发货，运单号：SF1234567890', 0),
(3, 1, '退款进度', '您的退款单 RF2026081200000001 正在等待商家处理', 1);