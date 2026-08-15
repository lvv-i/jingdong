package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商家订单列表项（T5 M-009：{id, orderNo, payAmount, status, receiverSnapshot, shippingNo, userName, createdAt}）
 * userName 买家昵称脱敏（C 增补）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantOrderVO {

    private Long id;

    private String orderNo;

    private BigDecimal payAmount;

    /** 状态：PENDING_PAY PAID SHIPPED COMPLETED CANCELLED */
    private String status;

    /** 收货信息快照 */
    private String receiverSnapshot;

    private String shippingNo;

    /** 买家昵称（脱敏） */
    private String userName;

    private LocalDateTime createdAt;
}
