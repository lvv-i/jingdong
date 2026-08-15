package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商家商品列表项（T5 M-004：{id, title, price, stock, status, salesCount}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantProductVO {

    private Long id;

    private String title;

    private BigDecimal price;

    private Integer stock;

    /** 状态：DRAFT PENDING_ON_SALE ON_SALE OFF_SALE */
    private String status;

    private Integer salesCount;
}
