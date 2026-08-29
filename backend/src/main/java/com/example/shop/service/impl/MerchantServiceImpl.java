package com.example.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shop.common.BusinessException;
import com.example.shop.common.ErrorCode;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.dto.ProductSaveDTO;
import com.example.shop.dto.ShipDTO;
import com.example.shop.dto.ShopUpdateDTO;
import com.example.shop.dto.StockUpdateDTO;
import com.example.shop.entity.AuditLog;
import com.example.shop.entity.Category;
import com.example.shop.entity.MerchantShop;
import com.example.shop.entity.Notice;
import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;
import com.example.shop.entity.Product;
import com.example.shop.entity.ProductImage;
import com.example.shop.entity.User;
import com.example.shop.enums.MerchantAuditStatus;
import com.example.shop.enums.OrderStatus;
import com.example.shop.enums.ProductStatus;
import com.example.shop.mapper.AuditLogMapper;
import com.example.shop.mapper.CategoryMapper;
import com.example.shop.mapper.MerchantShopMapper;
import com.example.shop.mapper.NoticeMapper;
import com.example.shop.mapper.OrderItemMapper;
import com.example.shop.mapper.OrderMapper;
import com.example.shop.mapper.ProductImageMapper;
import com.example.shop.mapper.ProductMapper;
import com.example.shop.mapper.UserMapper;
import com.example.shop.security.LoginUser;
import com.example.shop.security.UserContext;
import com.example.shop.service.MerchantService;
import com.example.shop.vo.MerchantOrderVO;
import com.example.shop.vo.MerchantProductVO;
import com.example.shop.vo.OrderDetailVO;
import com.example.shop.vo.OrderItemVO;
import com.example.shop.vo.ShopVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商家服务实现
 * M-001~M-011；数据范围：仅本店（T4 数据权限清单 #2，merchant_id==shopId）
 * M-011 发货写 audit_logs(SHIP)（T4 敏感操作留痕）
 */
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantShopMapper merchantShopMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final CategoryMapper categoryMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;
    private final AuditLogMapper auditLogMapper;
    private final NoticeMapper noticeMapper;

    @Override
    public ShopVO getShop() {
        MerchantShop shop = requireShop();
        return toShopVO(shop);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShop(ShopUpdateDTO dto) {
        MerchantShop shop = requireShop();
        // M-002：非 APPROVED 状态不可编辑（6003 审核中 / 6005 已驳回）
        if (!MerchantAuditStatus.APPROVED.name().equals(shop.getAuditStatus())) {
            throw new BusinessException(
                    MerchantAuditStatus.PENDING_AUDIT.name().equals(shop.getAuditStatus())
                            ? ErrorCode.SHOP_AUDITING : ErrorCode.SHOP_REJECTED);
        }
        // 主营类目校验（3004）
        if (categoryMapper.selectById(dto.getCategoryId()) == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        shop.setShopName(dto.getShopName());
        shop.setCategoryId(dto.getCategoryId());
        shop.setDescription(dto.getDescription());
        merchantShopMapper.updateById(shop);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resubmitShop() {
        MerchantShop shop = requireShop();
        // T1 3.2：仅 REJECTED 可修改后重新提交（状态不符 1001）
        if (!MerchantAuditStatus.REJECTED.name().equals(shop.getAuditStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "店铺当前状态不可重新提交");
        }
        shop.setAuditStatus(MerchantAuditStatus.PENDING_AUDIT.name());
        shop.setAuditReason(null);
        merchantShopMapper.updateById(shop);
        // 审计留痕（T4：入驻重提）
        AuditLog log = new AuditLog();
        log.setOperatorId(UserContext.requireUserId());
        log.setOperatorRole(LoginUser.ROLE_MERCHANT);
        log.setTargetType("SHOP");
        log.setTargetId(shop.getId());
        log.setAction("RESUBMIT");
        log.setRemark("商家重新提交入驻审核");
        auditLogMapper.insert(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProduct(ProductSaveDTO dto) {
        MerchantShop shop = requireShop();
        // M-003：店铺须已通过审核（6002）
        requireApprovedShop(shop);
        // 类目校验（3004）
        if (categoryMapper.selectById(dto.getCategoryId()) == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        Product product = new Product();
        product.setMerchantId(shop.getId());
        fillProduct(product, dto);
        product.setSalesCount(0);
        product.setStatus(ProductStatus.DRAFT.name());
        productMapper.insert(product);
        saveImages(product.getId(), dto.getImages());
        return product.getId();
    }

    @Override
    public PageResult<MerchantProductVO> listProducts(PageQuery pageQuery, String status, String keyword) {
        Long shopId = requireShopId();
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, shopId)
                .orderByDesc(Product::getCreatedAt);
        if (StringUtils.hasText(status)) {
            if (!ProductStatus.isValid(status)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "商品状态值非法");
            }
            wrapper.eq(Product::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Product::getTitle, keyword).or().like(Product::getSubTitle, keyword));
        }
        Page<Product> page = productMapper.selectPage(
                new Page<>(pageQuery.getPage(), pageQuery.getPageSize()), wrapper);
        List<MerchantProductVO> list = page.getRecords().stream().map(p -> new MerchantProductVO(
                p.getId(), p.getTitle(), p.getPrice(), p.getStock(), p.getStatus(), p.getSalesCount())).toList();
        return PageResult.of(list, page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Long id, ProductSaveDTO dto) {
        Product product = requireOwnProduct(id);
        // M-005：仅 DRAFT/OFF_SALE 可编辑（3005）
        if (!List.of(ProductStatus.DRAFT.name(), ProductStatus.OFF_SALE.name()).contains(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_STATUS_NOT_ALLOWED);
        }
        if (categoryMapper.selectById(dto.getCategoryId()) == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        fillProduct(product, dto);
        productMapper.updateById(product);
        // 多图全量替换
        productImageMapper.delete(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getProductId, id));
        saveImages(id, dto.getImages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitProduct(Long id) {
        Product product = requireOwnProduct(id);
        // M-006：DRAFT / OFF_SALE → PENDING_ON_SALE（T1 4.2：下架商品重新提交上架须重新审核）
        if (!List.of(ProductStatus.DRAFT.name(), ProductStatus.OFF_SALE.name()).contains(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_STATUS_NOT_ALLOWED);
        }
        // 3006 信息不完整校验（上架必填字段）
        if (product.getCategoryId() == null || !StringUtils.hasText(product.getTitle())
                || product.getPrice() == null || product.getStock() == null
                || !StringUtils.hasText(product.getMainImage()) || !StringUtils.hasText(product.getDetail())) {
            throw new BusinessException(ErrorCode.PRODUCT_INFO_INCOMPLETE);
        }
        product.setStatus(ProductStatus.PENDING_ON_SALE.name());
        productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offProduct(Long id) {
        Product product = requireOwnProduct(id);
        // M-007：ON_SALE → OFF_SALE（商家主动下架）
        if (!ProductStatus.ON_SALE.name().equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_STATUS_NOT_ALLOWED);
        }
        product.setStatus(ProductStatus.OFF_SALE.name());
        productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStock(Long id, StockUpdateDTO dto) {
        Product product = requireOwnProduct(id);
        product.setStock(dto.getStock());
        productMapper.updateById(product);
    }

    @Override
    public PageResult<MerchantOrderVO> listOrders(PageQuery pageQuery, String status) {
        Long shopId = requireShopId();
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, shopId)
                .orderByDesc(Order::getCreatedAt);
        if (StringUtils.hasText(status)) {
            if (!OrderStatus.isValid(status)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "订单状态值非法");
            }
            wrapper.eq(Order::getStatus, status);
        }
        Page<Order> page = orderMapper.selectPage(
                new Page<>(pageQuery.getPage(), pageQuery.getPageSize()), wrapper);

        // 买家昵称批量查（脱敏，C 增补）
        List<Long> userIds = page.getRecords().stream().map(Order::getUserId).distinct().toList();
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<MerchantOrderVO> list = page.getRecords().stream().map(o -> {
            User buyer = userMap.get(o.getUserId());
            return new MerchantOrderVO(o.getId(), o.getOrderNo(), o.getPayAmount(), o.getStatus(),
                    o.getReceiverSnapshot(), o.getShippingNo(),
                    buyer == null ? "未知用户" : maskUsername(buyer.getUsername()), o.getCreatedAt());
        }).toList();
        return PageResult.of(list, page.getTotal());
    }

    @Override
    public OrderDetailVO orderDetail(Long id) {
        Long shopId = requireShopId();
        Order order = orderMapper.selectById(id);
        // 4001 订单不存在；6004 非本店
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getMerchantId() == null || !order.getMerchantId().equals(shopId)) {
            throw new BusinessException(ErrorCode.NOT_MY_SHOP_DATA);
        }
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, id).orderByAsc(OrderItem::getId));
        List<OrderItemVO> itemVOs = items.stream().map(i -> new OrderItemVO(i.getId(), i.getProductId(),
                i.getTitleSnapshot(), i.getPriceSnapshot(), i.getQuantity(), i.getTotalPrice(),
                i.getRating(), i.getComment(), i.getReviewedAt())).toList();
        boolean reviewed = !itemVOs.isEmpty() && itemVOs.stream().allMatch(i -> i.getRating() != null);
        return new OrderDetailVO(order.getId(), order.getOrderNo(), order.getTotalAmount(), order.getPayAmount(),
                order.getReceiverSnapshot(), order.getShippingNo(), order.getStatus(), itemVOs, reviewed,
                order.getPaidAt(), order.getShippedAt(), order.getCompletedAt(), order.getCreatedAt());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long id, ShipDTO dto) {
        Long shopId = requireShopId();
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getMerchantId() == null || !order.getMerchantId().equals(shopId)) {
            throw new BusinessException(ErrorCode.NOT_MY_SHOP_DATA);
        }
        // 4009 运单号必填（DTO 已校验，服务层兜底）
        if (!StringUtils.hasText(dto.getShippingNo())) {
            throw new BusinessException(ErrorCode.SHIPPING_NO_REQUIRED);
        }
        // 4002 仅 PAID 可发货（T1：PAID → SHIPPED）
        if (!OrderStatus.PAID.name().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_ALLOWED);
        }
        order.setStatus(OrderStatus.SHIPPED.name());
        order.setShippingNo(dto.getShippingNo());
        order.setShippedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 审计留痕（T4 强制清单：SHIP）
        AuditLog log = new AuditLog();
        log.setOperatorId(UserContext.requireUserId());
        log.setOperatorRole(LoginUser.ROLE_MERCHANT);
        log.setTargetType("ORDER");
        log.setTargetId(order.getId());
        log.setAction("SHIP");
        log.setRemark("发货运单号：" + dto.getShippingNo());
        auditLogMapper.insert(log);

        // 通知买家
        Notice notice = new Notice();
        notice.setReceiverId(order.getUserId());
        notice.setTitle("订单发货");
        notice.setContent("您的订单 " + order.getOrderNo() + " 已发货，运单号：" + dto.getShippingNo());
        notice.setReadStatus(0);
        noticeMapper.insert(notice);
    }

    // ---------- 私有工具 ----------

    /** 当前商家店铺（JWT 签发 shopId；异常场景兜底 6001） */
    private MerchantShop requireShop() {
        MerchantShop shop = merchantShopMapper.selectById(requireShopId());
        if (shop == null) {
            throw new BusinessException(ErrorCode.SHOP_NOT_FOUND);
        }
        return shop;
    }

    private Long requireShopId() {
        LoginUser user = UserContext.get();
        Long shopId = user == null ? null : user.getShopId();
        if (shopId == null) {
            throw new BusinessException(ErrorCode.SHOP_NOT_FOUND);
        }
        return shopId;
    }

    /** 店铺须已通过审核（6002） */
    private void requireApprovedShop(MerchantShop shop) {
        if (!MerchantAuditStatus.APPROVED.name().equals(shop.getAuditStatus())) {
            throw new BusinessException(ErrorCode.SHOP_NOT_APPROVED);
        }
    }

    /** 商品归属本店校验（3001 不存在 / 6004 非本店） */
    private Product requireOwnProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (product.getMerchantId() == null || !product.getMerchantId().equals(requireShopId())) {
            throw new BusinessException(ErrorCode.NOT_MY_SHOP_DATA);
        }
        return product;
    }

    private void fillProduct(Product product, ProductSaveDTO dto) {
        product.setCategoryId(dto.getCategoryId());
        product.setTitle(dto.getTitle());
        product.setSubTitle(dto.getSubTitle());
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setStock(dto.getStock());
        product.setMainImage(dto.getMainImage());
        product.setDetail(dto.getDetail());
    }

    /** 多图写入 product_images（sort_order 按提交顺序） */
    private void saveImages(Long productId, List<String> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        int sort = 1;
        for (String url : images) {
            if (!StringUtils.hasText(url)) {
                continue;
            }
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setImageUrl(url);
            image.setSortOrder(sort++);
            productImageMapper.insert(image);
        }
    }

    private ShopVO toShopVO(MerchantShop shop) {
        return new ShopVO(shop.getId(), shop.getShopName(), shop.getCategoryId(),
                shop.getDescription(), shop.getAuditStatus(), shop.getAuditReason());
    }

    /** 用户名脱敏：保留首尾字符，中间打码（如 user001 → u***1） */
    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) {
            return "**";
        }
        return username.charAt(0) + "***" + username.charAt(username.length() - 1);
    }
}
