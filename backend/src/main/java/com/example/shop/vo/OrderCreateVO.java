package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单结果（T5 U-012 拆单适配：按商家拆单，data 为 {orders:[...]}）
 * 单条：{orderId, orderNo, status:PENDING_PAY, payAmount}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateVO {

    private List<OrderBriefVO> orders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderBriefVO {
        private Long orderId;
        private String orderNo;
        private String status;
        private BigDecimal payAmount;
    }
}
