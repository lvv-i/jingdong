package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 售后单列表项（T5 U-019/M-012：{id, refundNo, orderId, refundAmount, reason, status, merchantReply, adminResult, createdAt}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundListItemVO {

    private Long id;

    private String refundNo;

    private Long orderId;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 退款原因（用户填写） */
    private String reason;

    /** 状态：REFUNDING MERCHANT_AGREED MERCHANT_REJECTED ADMIN_INTERVENED REFUNDED CLOSED */
    private String status;

    /** 商家回复（留痕） */
    private String merchantReply;

    /** 管理员裁决结果（留痕） */
    private String adminResult;

    private LocalDateTime createdAt;
}
