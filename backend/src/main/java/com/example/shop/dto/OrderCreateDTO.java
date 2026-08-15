package com.example.shop.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建订单请求（T5 U-012：{addressId, cartItemIds, remark}）
 * 按商家拆单；校验库存与价格快照；快照写入 order_items
 */
@Data
public class OrderCreateDTO {

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @NotEmpty(message = "请勾选要结算的商品")
    private List<Long> cartItemIds;

    /** 订单备注（可选） */
    private String remark;
}
