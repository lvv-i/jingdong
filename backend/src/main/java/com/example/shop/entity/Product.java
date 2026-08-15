package com.example.shop.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品主表实体（数据字典 2.4 products）
 * status：DRAFT草稿 PENDING_ON_SALE待上架 ON_SALE已上架 OFF_SALE已下架（T1 商品状态机）
 */
@Data
@TableName("products")
public class Product {

    /** 商品ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商家ID */
    private Long merchantId;

    /** 类目ID */
    private Long categoryId;

    /** 商品标题 */
    private String title;

    /** 商品副标题 */
    private String subTitle;

    /** 售价 */
    private BigDecimal price;

    /** 原价（划线价，可为空） */
    private BigDecimal originalPrice;

    /** 库存数量 */
    private Integer stock;

    /** 累计销量 */
    private Integer salesCount;

    /** 主图URL */
    private String mainImage;

    /** 商品详情（富文本） */
    private String detail;

    /** 状态：DRAFT PENDING_ON_SALE ON_SALE OFF_SALE */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 软删除：0未删除 1已删除 */
    private Integer deletedFlag;
}
