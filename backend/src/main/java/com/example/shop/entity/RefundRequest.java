package com.example.shop.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款申请表实体（数据字典 2.9 refund_requests）
 * status：REFUNDING退款中 MERCHANT_AGREED商家同意 MERCHANT_REJECTED商家拒绝
 *         ADMIN_INTERVENED平台介入 REFUNDED已退款 CLOSED已关闭（T1 售后状态机）
 * 留痕必存：reason / merchant_reply / admin_result
 */
@Data
@TableName("refund_requests")
public class RefundRequest {

    /** 退款单ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 退款单编号 */
    private String refundNo;

    /** 订单ID */
    private Long orderId;

    /** 申请用户ID */
    private Long userId;

    /** 商家ID */
    private Long merchantId;

    /** 退款金额（≤订单实付金额，后端校验） */
    private BigDecimal refundAmount;

    /** 退款原因（用户填写） */
    private String reason;

    /** 商家回复（同意/拒绝意见，留痕） */
    private String merchantReply;

    /** 管理员裁决结果（平台介入，留痕） */
    private String adminResult;

    /** 状态：REFUNDING MERCHANT_AGREED MERCHANT_REJECTED ADMIN_INTERVENED REFUNDED CLOSED */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 软删除：0未删除 1已删除 */
    private Integer deletedFlag;
}
