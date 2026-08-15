package com.example.shop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 修改购物车项请求（T5 U-010：{quantity, selected}）
 */
@Data
public class CartUpdateDTO {

    @Min(value = 1, message = "数量至少为 1")
    @Max(value = 999, message = "数量过大")
    private Integer quantity;

    /** 是否勾选结算：0未选 1已选 */
    @Min(value = 0, message = "selected 只能为 0 或 1")
    @Max(value = 1, message = "selected 只能为 0 或 1")
    private Integer selected;
}
