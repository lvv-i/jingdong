package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 购物车项（T5 U-008：{id, productId, title, price, quantity, selected, stock}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemVO {

    /** 购物车条目ID */
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 商品标题（当前价实时读取） */
    private String title;

    /** 当前售价 */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;

    /** 是否勾选结算：0未选 1已选 */
    private Integer selected;

    /** 当前库存（前端提示库存不足） */
    private Integer stock;
}
