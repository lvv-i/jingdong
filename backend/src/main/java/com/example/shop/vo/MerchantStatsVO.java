package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商家店铺仪表盘统计（T5 M-015，C 增补：
 * {todayOrderCount, todaySalesAmount, pendingShipCount, pendingRefundCount, productCount}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantStatsVO {

    /** 今日订单数 */
    private Long todayOrderCount;

    /** 今日销售额 */
    private BigDecimal todaySalesAmount;

    /** 待发货订单数 */
    private Long pendingShipCount;

    /** 待处理退款数 */
    private Long pendingRefundCount;

    /** 商品总数 */
    private Long productCount;
}
