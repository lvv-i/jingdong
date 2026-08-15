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
 * 订单主表实体（数据字典 2.7 orders）
 * status：PENDING_PAY待支付 PAID已支付待发货 SHIPPED已发货待收货 COMPLETED已完成 CANCELLED已取消（T1 订单状态机）
 * 一单一商家（购物车按商家拆单）；收货信息快照下单时冻结
 */
@Data
@TableName("orders")
public class Order {

    /** 订单ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 下单用户ID */
    private Long userId;

    /** 商家ID（一单一商家，跨店拆单） */
    private Long merchantId;

    /** 商品总金额 */
    private BigDecimal totalAmount;

    /** 实付金额 */
    private BigDecimal payAmount;

    /** 收货信息快照（收件人+电话+完整地址，下单时冻结） */
    private String receiverSnapshot;

    /** 运单号（发货时必填） */
    private String shippingNo;

    /** 支付时间 */
    private LocalDateTime paidAt;

    /** 发货时间 */
    private LocalDateTime shippedAt;

    /** 完成时间 */
    private LocalDateTime completedAt;

    /** 取消时间 */
    private LocalDateTime cancelledAt;

    /** 状态：PENDING_PAY PAID SHIPPED COMPLETED CANCELLED */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 软删除：0未删除 1已删除 */
    private Integer deletedFlag;
}
