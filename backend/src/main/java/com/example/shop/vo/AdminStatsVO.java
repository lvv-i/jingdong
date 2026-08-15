package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 管理员仪表盘统计（T5 A-019：
 * {userCount, merchantCount, productCount, orderCount, refundCount, salesAmount}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsVO {

    private Long userCount;

    private Long merchantCount;

    private Long productCount;

    private Long orderCount;

    private Long refundCount;

    /** 累计销售额（已支付订单实付金额合计） */
    private BigDecimal salesAmount;
}
