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
 * 支付流水表实体（数据字典 2.10 payment_records，扩展表 2）
 * type：PAY支付 REFUND退款；status：SUCCESS成功 FAILED失败
 */
@Data
@TableName("payment_records")
public class PaymentRecord {

    /** 流水ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 支付流水号 */
    private String paymentNo;

    /** 订单ID */
    private Long orderId;

    /** 支付用户ID */
    private Long userId;

    /** 关联退款单ID（退款流水时填写） */
    private Long refundId;

    /** 金额 */
    private BigDecimal amount;

    /** 流水类型：PAY REFUND */
    private String type;

    /** 状态：SUCCESS FAILED */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 软删除：0未删除 1已删除 */
    private Integer deletedFlag;
}
