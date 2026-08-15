package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单明细项（T5 U-014 items：{id, productId, titleSnapshot, priceSnapshot, quantity, totalPrice, rating, comment, reviewedAt}）
 * 快照字段：title_snapshot / price_snapshot（下单时冻结，商品改价改名不影响历史订单）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemVO {

    private Long id;

    private Long productId;

    /** 商品标题快照 */
    private String titleSnapshot;

    /** 成交单价快照 */
    private BigDecimal priceSnapshot;

    private Integer quantity;

    /** 小计金额 */
    private BigDecimal totalPrice;

    /** 评价评分 1-5（NULL=未评价） */
    private Integer rating;

    /** 评价内容 */
    private String comment;

    /** 评价时间 */
    private java.time.LocalDateTime reviewedAt;
}
