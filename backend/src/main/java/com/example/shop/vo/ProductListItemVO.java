package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品列表项（T5 P-004：{id, title, price, originalPrice, salesCount, mainImage, status}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductListItemVO {

    private Long id;

    private String title;

    private BigDecimal price;

    /** 原价（划线价） */
    private BigDecimal originalPrice;

    /** 累计销量 */
    private Integer salesCount;

    /** 主图URL */
    private String mainImage;

    /** 状态（公开接口恒为 ON_SALE） */
    private String status;
}
