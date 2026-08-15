package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品巡检/审核列表项（T5 A-008：{id, title, price, stock, status, merchantName}；
 * A-010 增补 createdAt）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductVO {

    private Long id;

    private String title;

    private BigDecimal price;

    private Integer stock;

    /** 状态：DRAFT PENDING_ON_SALE ON_SALE OFF_SALE */
    private String status;

    /** 所属店铺名 */
    private String merchantName;

    private LocalDateTime createdAt;
}
