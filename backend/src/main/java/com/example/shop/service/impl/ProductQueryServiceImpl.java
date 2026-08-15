package com.example.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shop.common.BusinessException;
import com.example.shop.common.ErrorCode;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.entity.Category;
import com.example.shop.entity.MerchantShop;
import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;
import com.example.shop.entity.Product;
import com.example.shop.entity.ProductImage;
import com.example.shop.entity.User;
import com.example.shop.enums.ProductStatus;
import com.example.shop.mapper.CategoryMapper;
import com.example.shop.mapper.MerchantShopMapper;
import com.example.shop.mapper.OrderItemMapper;
import com.example.shop.mapper.OrderMapper;
import com.example.shop.mapper.ProductImageMapper;
import com.example.shop.mapper.ProductMapper;
import com.example.shop.mapper.UserMapper;
import com.example.shop.service.ProductQueryService;
import com.example.shop.vo.ProductDetailVO;
import com.example.shop.vo.ProductListItemVO;
import com.example.shop.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 公开商品查询服务实现
 * P-004/P-005/P-006
 */
@Service
@RequiredArgsConstructor
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final MerchantShopMapper merchantShopMapper;
    private final CategoryMapper categoryMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<ProductListItemVO> list(PageQuery pageQuery, Long categoryId, String keyword,
                                              Long merchantId, String sort) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, ProductStatus.ON_SALE.name());

        // 类目过滤：含子类目（B 端类目导航习惯）
        if (categoryId != null) {
            List<Long> categoryIds = collectCategoryIds(categoryId);
            wrapper.in(Product::getCategoryId, categoryIds);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Product::getTitle, keyword).or().like(Product::getSubTitle, keyword));
        }
        if (merchantId != null) {
            wrapper.eq(Product::getMerchantId, merchantId);
        }

        // 排序：综合(默认)/sales/priceAsc/priceDesc（T5 B 增补）
        switch (sort == null ? "" : sort) {
            case "sales" -> wrapper.orderByDesc(Product::getSalesCount);
            case "priceAsc" -> wrapper.orderByAsc(Product::getPrice);
            case "priceDesc" -> wrapper.orderByDesc(Product::getPrice);
            default -> wrapper.orderByDesc(Product::getSalesCount).orderByDesc(Product::getId);
        }

        Page<Product> page = productMapper.selectPage(
                new Page<>(pageQuery.getPage(), pageQuery.getPageSize()), wrapper);
        List<ProductListItemVO> list = page.getRecords().stream().map(p -> new ProductListItemVO(
                p.getId(), p.getTitle(), p.getPrice(), p.getOriginalPrice(),
                p.getSalesCount(), p.getMainImage(), p.getStatus())).toList();
        return PageResult.of(list, page.getTotal());
    }

    @Override
    public ProductDetailVO detail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        // 非 ON_SALE 商品用户端不可见（T5 P-005 备注 → 3002）
        if (!ProductStatus.ON_SALE.name().equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_OFF_SALE);
        }
        // 多图
        List<String> images = productImageMapper.selectList(new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, id)
                        .orderByAsc(ProductImage::getSortOrder))
                .stream().map(ProductImage::getImageUrl).toList();
        // 店铺名
        String merchantName = "";
        MerchantShop shop = merchantShopMapper.selectById(product.getMerchantId());
        if (shop != null) {
            merchantName = shop.getShopName();
        }
        return new ProductDetailVO(product.getId(), product.getTitle(), product.getSubTitle(),
                product.getPrice(), product.getOriginalPrice(), product.getStock(), product.getSalesCount(),
                product.getMainImage(), product.getDetail(), images, merchantName);
    }

    @Override
    public PageResult<ReviewVO> listReviews(Long productId, PageQuery pageQuery) {
        // 商品必须存在（3001）；非 ON_SALE 商品评价同样不可见（3002）
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (!ProductStatus.ON_SALE.name().equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_OFF_SALE);
        }
        // 仅已评价明细（rating 非空）
        Page<OrderItem> page = orderItemMapper.selectPage(
                new Page<>(pageQuery.getPage(), pageQuery.getPageSize()),
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getProductId, productId)
                        .isNotNull(OrderItem::getRating)
                        .orderByDesc(OrderItem::getReviewedAt));

        // 订单归属用户（order_items 无 user_id，经 orders 关联）
        List<Long> orderIds = page.getRecords().stream().map(OrderItem::getOrderId).distinct().toList();
        Map<Long, Long> orderUserMap = orderIds.isEmpty() ? Map.of()
                : orderMapper.selectBatchIds(orderIds).stream()
                .filter(o -> o != null && o.getUserId() != null)
                .collect(Collectors.toMap(Order::getId, Order::getUserId));

        // 批量查评价人用户名（脱敏）
        List<Long> userIds = orderUserMap.values().stream().distinct().toList();
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<ReviewVO> list = page.getRecords().stream().map(item -> {
            Long userId = orderUserMap.get(item.getOrderId());
            User user = userId == null ? null : userMap.get(userId);
            String userName = user == null ? "匿名用户" : maskUsername(user.getUsername());
            return new ReviewVO(userName, item.getRating(), item.getComment(), item.getReviewedAt());
        }).toList();
        return PageResult.of(list, page.getTotal());
    }

    /** 收集类目及其全部子类目 ID（含自身） */
    private List<Long> collectCategoryIds(Long categoryId) {
        List<Category> all = categoryMapper.selectList(null);
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);
        boolean added;
        do {
            added = false;
            for (Category c : all) {
                if (ids.contains(c.getParentId()) && !ids.contains(c.getId())) {
                    ids.add(c.getId());
                    added = true;
                }
            }
        } while (added);
        return ids;
    }

    /** 用户名脱敏：保留首尾字符，中间打码（如 user001 → u***1） */
    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) {
            return "**";
        }
        return username.charAt(0) + "***" + username.charAt(username.length() - 1);
    }
}
