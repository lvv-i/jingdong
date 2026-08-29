package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情（T5 U-014：{id, orderNo, totalAmount, payAmount, receiverSnapshot, shippingNo, status,
 * items:[...], paidAt, shippedAt, completedAt, reviewed}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVO {

    private Long id;

    private String orderNo;

    /** 商品总金额 */
    private BigDecimal totalAmount;

    /** 实付金额 */
    private BigDecimal payAmount;

    /** 收货信息快照 */
    private String receiverSnapshot;

    /** 运单号（发货后） */
    private String shippingNo;

    private String status;

    /** 订单明细（快照） */
    private List<OrderItemVO> items;

    /** 订单是否已全部评价（所有明细细节均有 rating；U-024 评价后为 true） */
    private Boolean reviewed;

    private LocalDateTime paidAt;

    private LocalDateTime shippedAt;

    private LocalDateTime completedAt;

    private LocalDateTime createdAt;
}
