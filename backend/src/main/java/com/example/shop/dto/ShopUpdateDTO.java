package com.example.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家编辑店铺信息请求（T5 M-002：{shopName, categoryId, description}）
 * 非 APPROVED 状态不可编辑（6002/6003/6005）
 */
@Data
public class ShopUpdateDTO {

    @NotBlank(message = "店铺名称不能为空")
    @Size(max = 100, message = "店铺名称过长")
    private String shopName;

    @NotNull(message = "主营类目不能为空")
    private Long categoryId;

    /** 店铺简介（可选） */
    @Size(max = 500, message = "店铺简介过长")
    private String description;
}
