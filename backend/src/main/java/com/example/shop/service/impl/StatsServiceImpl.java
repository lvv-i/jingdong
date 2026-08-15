package com.example.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.shop.common.BusinessException;
import com.example.shop.common.ErrorCode;
import com.example.shop.entity.MerchantShop;
import com.example.shop.entity.Order;
import com.example.shop.entity.Product;
import com.example.shop.entity.RefundRequest;
import com.example.shop.entity.User;
import com.example.shop.enums.OrderStatus;
import com.example.shop.enums.RefundStatus;
import com.example.shop.mapper.MerchantShopMapper;
import com.example.shop.mapper.OrderMapper;
import com.example.shop.mapper.ProductMapper;
import com.example.shop.mapper.RefundRequestMapper;
import com.example.shop.mapper.UserMapper;
import com.example.shop.security.LoginUser;
import com.example.shop.security.UserContext;
import com.example.shop.service.StatsService;
import com.example.shop.vo.AdminStatsVO;
import com.example.shop.vo.MerchantStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 统计服务实现
 * M-015 店铺仪表盘（仅本店）/ A-019 平台仪表盘（全局）
 * 销售口径：已支付订单（paid_at 非空）实付金额合计
 */
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final RefundRequestMapper refundRequestMapper;
    private final UserMapper userMapper;
    private final MerchantShopMapper merchantShopMapper;

    @Override
    public MerchantStatsVO merchantStats() {
        Long shopId = requireShopId();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        long todayOrderCount = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, shopId)
                .ge(Order::getCreatedAt, todayStart));
        BigDecimal todaySalesAmount = sumOrderAmount(new QueryWrapper<Order>()
                .eq("merchant_id", shopId)
                .ge("paid_at", todayStart));
        long pendingShipCount = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, shopId)
                .eq(Order::getStatus, OrderStatus.PAID.name()));
        long pendingRefundCount = refundRequestMapper.selectCount(new LambdaQueryWrapper<RefundRequest>()
                .eq(RefundRequest::getMerchantId, shopId)
                .eq(RefundRequest::getStatus, RefundStatus.REFUNDING.name()));
        long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, shopId));

        return new MerchantStatsVO(todayOrderCount, todaySalesAmount,
                pendingShipCount, pendingRefundCount, productCount);
    }

    @Override
    public AdminStatsVO adminStats() {
        long userCount = userMapper.selectCount(null);
        long merchantCount = merchantShopMapper.selectCount(null);
        long productCount = productMapper.selectCount(null);
        long orderCount = orderMapper.selectCount(null);
        long refundCount = refundRequestMapper.selectCount(null);
        BigDecimal salesAmount = sumOrderAmount(new QueryWrapper<Order>()
                .in("status", OrderStatus.PAID.name(), OrderStatus.SHIPPED.name(), OrderStatus.COMPLETED.name()));

        return new AdminStatsVO(userCount, merchantCount, productCount,
                orderCount, refundCount, salesAmount);
    }

    // ---------- 私有工具 ----------

    /** SUM(pay_amount) 聚合（无记录返回 0） */
    private BigDecimal sumOrderAmount(QueryWrapper<Order> wrapper) {
        wrapper.select("COALESCE(SUM(pay_amount), 0) AS total");
        List<Object> result = orderMapper.selectObjs(wrapper);
        Object value = result.isEmpty() ? null : result.get(0);
        return value == null ? BigDecimal.ZERO : new BigDecimal(value.toString());
    }

    /** 当前商家店铺ID（JWT 签发；异常场景兜底 6001） */
    private Long requireShopId() {
        LoginUser user = UserContext.get();
        Long shopId = user == null ? null : user.getShopId();
        if (shopId == null) {
            throw new BusinessException(ErrorCode.SHOP_NOT_FOUND);
        }
        return shopId;
    }
}
