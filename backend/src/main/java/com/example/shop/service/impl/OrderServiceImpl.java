package com.example.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shop.common.BusinessException;
import com.example.shop.common.ErrorCode;
import com.example.shop.common.IdGenerator;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.dto.OrderCreateDTO;
import com.example.shop.dto.ReviewCreateDTO;
import com.example.shop.entity.Address;
import com.example.shop.entity.CartItem;
import com.example.shop.entity.Notice;
import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;
import com.example.shop.entity.PaymentRecord;
import com.example.shop.entity.Product;
import com.example.shop.enums.OrderStatus;
import com.example.shop.enums.ProductStatus;
import com.example.shop.mapper.AddressMapper;
import com.example.shop.mapper.CartItemMapper;
import com.example.shop.mapper.NoticeMapper;
import com.example.shop.mapper.OrderItemMapper;
import com.example.shop.mapper.OrderMapper;
import com.example.shop.mapper.PaymentRecordMapper;
import com.example.shop.mapper.ProductMapper;
import com.example.shop.security.UserContext;
import com.example.shop.service.OrderService;
import com.example.shop.vo.OrderCreateVO;
import com.example.shop.vo.OrderDetailVO;
import com.example.shop.vo.OrderItemVO;
import com.example.shop.vo.OrderListItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户订单服务实现（T1 订单状态机核心）
 * U-012/U-013/U-014/U-015/U-016/U-017/U-024 + 支付超时自动取消定时任务
 * 数据权限：订单归属本人（T4 清单 #1）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final NoticeMapper noticeMapper;

    @Value("${order.pay-timeout-minutes:30}")
    private long payTimeoutMinutes;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO create(OrderCreateDTO dto) {
        Long userId = UserContext.requireUserId();

        // 1. 地址校验（本人，4005）
        Address address = addressMapper.selectById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_ADDRESS_NOT_FOUND);
        }
        // 2. 购物车为空（4004）
        if (dto.getCartItemIds() == null || dto.getCartItemIds().isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }
        // 3. 加载购物车项（归属本人，4003）
        List<CartItem> cartItems = cartItemMapper.selectBatchIds(dto.getCartItemIds());
        if (cartItems.size() != dto.getCartItemIds().size()) {
            throw new BusinessException(ErrorCode.CART_ITEM_INVALID);
        }
        for (CartItem item : cartItems) {
            if (!item.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.CART_ITEM_INVALID);
            }
        }
        // 4. 商品校验：存在/在售/库存（3001/3002/4006）
        List<Product> products = new ArrayList<>();
        for (CartItem item : cartItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            if (!ProductStatus.ON_SALE.name().equals(product.getStatus())) {
                throw new BusinessException(ErrorCode.PRODUCT_OFF_SALE, "商品「" + product.getTitle() + "」已下架");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH, "商品「" + product.getTitle() + "」库存不足");
            }
            products.add(product);
        }

        // 5. 按商家拆单（T2：一单一商家）
        Map<Long, List<CartItem>> byMerchant = cartItems.stream()
                .collect(Collectors.groupingBy(item -> productOf(item.getProductId(), products).getMerchantId()));

        String snapshot = buildReceiverSnapshot(address);
        List<OrderCreateVO.OrderBriefVO> briefs = new ArrayList<>();
        for (Map.Entry<Long, List<CartItem>> entry : byMerchant.entrySet()) {
            Long merchantId = entry.getKey();
            List<CartItem> group = entry.getValue();

            // 计算金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (CartItem item : group) {
                Product p = productOf(item.getProductId(), products);
                totalAmount = totalAmount.add(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            Order order = new Order();
            order.setOrderNo(IdGenerator.orderNo());
            order.setUserId(userId);
            order.setMerchantId(merchantId);
            order.setTotalAmount(totalAmount);
            order.setPayAmount(totalAmount);
            order.setReceiverSnapshot(snapshot);
            order.setStatus(OrderStatus.PENDING_PAY.name());
            orderMapper.insert(order);

            // 快照明细 + 条件扣库存（防超卖）
            for (CartItem item : group) {
                Product p = productOf(item.getProductId(), products);
                int qty = item.getQuantity();
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getId());
                orderItem.setProductId(p.getId());
                orderItem.setTitleSnapshot(p.getTitle());
                orderItem.setPriceSnapshot(p.getPrice());
                orderItem.setQuantity(qty);
                orderItem.setTotalPrice(p.getPrice().multiply(BigDecimal.valueOf(qty)));
                orderItemMapper.insert(orderItem);

                int updated = productMapper.update(null, new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, p.getId())
                        .ge(Product::getStock, qty)
                        .setSql("stock = stock - " + qty));
                if (updated == 0) {
                    throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH, "商品「" + p.getTitle() + "」库存不足");
                }
            }
            briefs.add(new OrderCreateVO.OrderBriefVO(order.getId(), order.getOrderNo(),
                    order.getStatus(), order.getPayAmount()));
        }

        // 6. 移除已结算购物车项
        cartItemMapper.deleteBatchIds(dto.getCartItemIds());
        return new OrderCreateVO(briefs);
    }

    @Override
    public PageResult<OrderListItemVO> list(PageQuery pageQuery, String status) {
        Long userId = UserContext.requireUserId();
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreatedAt);
        if (StringUtils.hasText(status)) {
            if (!OrderStatus.isValid(status)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "订单状态值非法");
            }
            wrapper.eq(Order::getStatus, status);
        }
        Page<Order> page = orderMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery.getPageSize()), wrapper);

        // 明细数与首图（批量查，避免 N+1）
        List<Long> orderIds = page.getRecords().stream().map(Order::getId).toList();
        Map<Long, List<OrderItem>> itemsMap = orderIds.isEmpty() ? Map.of()
                : orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                        .in(OrderItem::getOrderId, orderIds)
                        .orderByAsc(OrderItem::getId))
                .stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
        Map<Long, String> firstImageMap = firstImages(itemsMap);

        List<OrderListItemVO> list = page.getRecords().stream().map(o -> {
            List<OrderItem> items = itemsMap.getOrDefault(o.getId(), List.of());
            boolean reviewed = !items.isEmpty() && items.stream().allMatch(i -> i.getRating() != null);
            return new OrderListItemVO(o.getId(), o.getOrderNo(), o.getPayAmount(), o.getStatus(),
                    o.getCreatedAt(), items.size(), firstImageMap.get(o.getId()), reviewed);
        }).toList();
        return PageResult.of(list, page.getTotal());
    }

    @Override
    public OrderDetailVO detail(Long id) {
        Order order = requireOwnOrder(id);
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, id).orderByAsc(OrderItem::getId));
        List<OrderItemVO> itemVOs = items.stream().map(i -> new OrderItemVO(i.getId(), i.getProductId(),
                i.getTitleSnapshot(), i.getPriceSnapshot(), i.getQuantity(), i.getTotalPrice(),
                i.getRating(), i.getComment(), i.getReviewedAt())).toList();
        boolean reviewed = !items.isEmpty() && items.stream().allMatch(i -> i.getRating() != null);
        return new OrderDetailVO(order.getId(), order.getOrderNo(), order.getTotalAmount(), order.getPayAmount(),
                order.getReceiverSnapshot(), order.getShippingNo(), order.getStatus(), itemVOs, reviewed,
                order.getPaidAt(), order.getShippedAt(), order.getCompletedAt(), order.getCreatedAt());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String pay(Long id) {
        Long userId = UserContext.requireUserId();
        Order order = requireOwnOrder(id);

        // 4008 已支付（幂等防重）
        if (OrderStatus.PAID.name().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PAID);
        }
        // 4002 仅待支付可支付
        if (!OrderStatus.PENDING_PAY.name().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_ALLOWED);
        }
        // 超时检查：超时自动取消（T1：创建后 N 分钟未支付）
        LocalDateTime deadline = order.getCreatedAt().plusMinutes(payTimeoutMinutes);
        if (LocalDateTime.now().isAfter(deadline)) {
            doCancelQuietly(order);
            throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_ALLOWED, "订单已超时未支付，系统已自动取消");
        }

        // 库存与价格快照校验（T1：库存充足；价格快照与当前价格一致 4007）
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, id));
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            if (!ProductStatus.ON_SALE.name().equals(product.getStatus())) {
                throw new BusinessException(ErrorCode.PRODUCT_OFF_SALE, "商品「" + item.getTitleSnapshot() + "」已下架");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BusinessException(ErrorCode.PRODUCT_STOCK_NOT_ENOUGH, "商品「" + item.getTitleSnapshot() + "」库存不足");
            }
            if (product.getPrice().compareTo(item.getPriceSnapshot()) != 0) {
                throw new BusinessException(ErrorCode.PRICE_CHANGED, "商品「" + item.getTitleSnapshot() + "」价格已变化");
            }
        }

        // 状态流转 PENDING_PAY → PAID
        order.setStatus(OrderStatus.PAID.name());
        order.setPaidAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 累计销量
        for (OrderItem item : items) {
            productMapper.update(null, new LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, item.getProductId())
                    .setSql("sales_count = sales_count + " + item.getQuantity()));
        }

        // 支付流水（PAY/SUCCESS）
        String paymentNo = IdGenerator.paymentNo();
        PaymentRecord record = new PaymentRecord();
        record.setPaymentNo(paymentNo);
        record.setOrderId(order.getId());
        record.setUserId(userId);
        record.setAmount(order.getPayAmount());
        record.setType("PAY");
        record.setStatus("SUCCESS");
        paymentRecordMapper.insert(record);

        // 站内通知：支付成功
        sendNotice(userId, "支付成功", "您的订单 " + order.getOrderNo() + " 已支付成功，等待商家发货");
        return paymentNo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        Order order = requireOwnOrder(id);
        // 4002 仅待支付可取消（T1）
        if (!OrderStatus.PENDING_PAY.name().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_ALLOWED);
        }
        doCancelQuietly(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(Long id) {
        Order order = requireOwnOrder(id);
        // 4002 SHIPPED → COMPLETED
        if (!OrderStatus.SHIPPED.name().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_ALLOWED);
        }
        order.setStatus(OrderStatus.COMPLETED.name());
        order.setCompletedAt(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(Long orderId, ReviewCreateDTO dto) {
        Order order = requireOwnOrder(orderId);
        // 仅 COMPLETED 可评价（4002）
        if (!OrderStatus.COMPLETED.name().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_ALLOWED, "仅已完成订单可评价");
        }
        OrderItem item = orderItemMapper.selectById(dto.getOrderItemId());
        // 4003 明细不存在或不属于该订单
        if (item == null || !item.getOrderId().equals(orderId)) {
            throw new BusinessException(ErrorCode.CART_ITEM_INVALID, "订单明细不存在");
        }
        // 已评价不可重复（4003）
        if (item.getRating() != null) {
            throw new BusinessException(ErrorCode.CART_ITEM_INVALID, "该商品已评价");
        }
        item.setRating(dto.getRating());
        item.setComment(dto.getComment());
        item.setReviewedAt(LocalDateTime.now());
        orderItemMapper.updateById(item);
    }

    /**
     * 支付超时自动取消定时任务（T1：创建后 pay-timeout-minutes 分钟未支付）
     * 每分钟执行一次；逐单取消（回补库存），不影响其他订单
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(payTimeoutMinutes);
        List<Order> timeoutOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, OrderStatus.PENDING_PAY.name())
                .lt(Order::getCreatedAt, deadline));
        for (Order order : timeoutOrders) {
            doCancelQuietly(order);
            log.info("[超时取消] 订单 {} 已自动取消", order.getOrderNo());
        }
    }

    /** 取消动作：状态置 CANCELLED + 回补库存 */
    private void doCancelQuietly(Order order) {
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem item : items) {
            productMapper.update(null, new LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, item.getProductId())
                    .setSql("stock = stock + " + item.getQuantity()));
        }
        order.setStatus(OrderStatus.CANCELLED.name());
        order.setCancelledAt(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    /** 校验订单归属本人（T4 数据权限清单 #1；4001） */
    private Order requireOwnOrder(Long id) {
        Order order = orderMapper.selectById(id);
        Long userId = UserContext.requireUserId();
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    private Product productOf(Long productId, List<Product> products) {
        return products.stream().filter(p -> p.getId().equals(productId)).findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    /** 收货信息快照（与 seed.sql 格式一致：收件人,电话,省市区详情） */
    private String buildReceiverSnapshot(Address address) {
        String district = address.getDistrict() == null ? "" : address.getDistrict();
        return String.join(",",
                address.getReceiver(), address.getPhone(),
                address.getProvince() + address.getCity() + district + address.getDetail());
    }

    /** 首商品主图（B 增补：列表缩略图） */
    private Map<Long, String> firstImages(Map<Long, List<OrderItem>> itemsMap) {
        Map<Long, String> result = new java.util.HashMap<>();
        for (Map.Entry<Long, List<OrderItem>> entry : itemsMap.entrySet()) {
            OrderItem first = entry.getValue().get(0);
            Product product = productMapper.selectById(first.getProductId());
            result.put(entry.getKey(), product == null ? null : product.getMainImage());
        }
        return result;
    }

    /** 站内通知 */
    private void sendNotice(Long receiverId, String title, String content) {
        Notice notice = new Notice();
        notice.setReceiverId(receiverId);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setReadStatus(0);
        noticeMapper.insert(notice);
    }
}
