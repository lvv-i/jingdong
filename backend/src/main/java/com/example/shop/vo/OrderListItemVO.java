package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单列表项（T5 U-013：{id, orderNo, payAmount, status, createdAt, itemsCount, firstItemImage, reviewed}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderListItemVO {

    private Long id;

    private String orderNo;

    /** 实付金额 */
    private BigDecimal payAmount;

    /** 状态：PENDING_PAY PAID SHIPPED COMPLETED CANCELLED */
    private String status;

    private LocalDateTime createdAt;

    /** 商品明细数 */
    private Integer itemsCount;

    /** 首商品主图（B 增补，用于列表缩略图） */
    private String firstItemImage;

    /** 订单是否已全部评价（所有明细细节均有 rating；U-024 评价后为 true） */
    private Boolean reviewed;
}
