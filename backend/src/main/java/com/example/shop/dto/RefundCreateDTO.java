package com.example.shop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 发起退款请求（T5 U-018：{orderId, reason, refundAmount}）
 * 订单 status∈{PAID,SHIPPED,COMPLETED}；无进行中售后单；金额≤实付（5006：reason 必填）
 */
@Data
public class RefundCreateDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotBlank(message = "退款原因不能为空")
    private String reason;

    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于 0")
    private BigDecimal refundAmount;
}
