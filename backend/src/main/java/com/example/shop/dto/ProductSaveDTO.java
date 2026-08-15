package com.example.shop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商家创建/编辑商品请求（T5 M-003/M-005）
 * 创建为草稿 DRAFT；images 写入 product_images；M-006 提交上架时校验必填字段（3006）
 */
@Data
public class ProductSaveDTO {

    @NotNull(message = "类目ID不能为空")
    private Long categoryId;

    @NotBlank(message = "商品标题不能为空")
    @Size(max = 200, message = "商品标题过长")
    private String title;

    /** 商品副标题（可选） */
    @Size(max = 200, message = "商品副标题过长")
    private String subTitle;

    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于 0")
    private BigDecimal price;

    /** 原价（划线价，可选） */
    @DecimalMin(value = "0.01", message = "原价必须大于 0")
    private BigDecimal originalPrice;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    @NotBlank(message = "主图不能为空")
    private String mainImage;

    /** 商品详情（富文本，可选） */
    private String detail;

    /** 商品多图（可选，写入 product_images） */
    private List<String> images;
}
