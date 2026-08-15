package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情（T5 P-005：{id, title, subTitle, price, originalPrice, stock, salesCount, mainImage, detail, images, merchantName}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailVO {

    private Long id;

    private String title;

    private String subTitle;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer stock;

    private Integer salesCount;

    private String mainImage;

    /** 商品详情（富文本） */
    private String detail;

    /** 商品多图（product_images） */
    private List<String> images;

    /** 店铺名称 */
    private String merchantName;
}
