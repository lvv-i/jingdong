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
 * 订单明细表实体（数据字典 2.8 order_items，含评价扩展字段）
 * 快照必存：title_snapshot / price_snapshot 下单时冻结，商品改价改名不影响历史订单
 * 评价扩展（T5 决议 #2）：rating 1-5 / comment ≤200字 / reviewed_at；仅订单 COMPLETED 后可写
 */
@Data
@TableName("order_items")
public class OrderItem {

    /** 明细ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 商品ID */
    private Long productId;

    /** 商品标题快照（下单时冻结） */
    private String titleSnapshot;

    /** 成交单价快照（下单时冻结） */
    private BigDecimal priceSnapshot;

    /** 购买数量 */
    private Integer quantity;

    /** 小计金额（price_snapshot × quantity） */
    private BigDecimal totalPrice;

    /** 评价评分 1-5（NULL=未评价；仅订单 COMPLETED 后可写） */
    private Integer rating;

    /** 评价内容（≤200字；rating 写入时必填） */
    private String comment;

    /** 评价时间 */
    private LocalDateTime reviewedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 软删除：0未删除 1已删除 */
    private Integer deletedFlag;
}
