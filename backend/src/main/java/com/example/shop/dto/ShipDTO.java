package com.example.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家发货请求（T5 M-011：{shippingNo}；PAID → SHIPPED；运单号必填 4009）
 */
@Data
public class ShipDTO {

    @NotBlank(message = "运单号不能为空")
    @Size(max = 50, message = "运单号过长")
    private String shippingNo;
}
