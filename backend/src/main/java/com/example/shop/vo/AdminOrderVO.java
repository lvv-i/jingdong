package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理员订单列表项（T5 A-013：{id, orderNo, userName, merchantName, payAmount, status}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderVO {

    private Long id;

    private String orderNo;

    /** 买家用户名 */
    private String userName;

    /** 店铺名 */
    private String merchantName;

    private BigDecimal payAmount;

    /** 状态：PENDING_PAY PAID SHIPPED COMPLETED CANCELLED */
    private String status;

    private LocalDateTime createdAt;
}
