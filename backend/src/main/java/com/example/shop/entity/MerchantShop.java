package com.example.shop.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家店铺表实体（数据字典 2.2 merchant_shops）
 * audit_status：PENDING_AUDIT待审核 APPROVED已通过 REJECTED已驳回（T1 入驻状态机）
 * status：NORMAL正常 CLOSED关店
 */
@Data
@TableName("merchant_shops")
public class MerchantShop {

    /** 店铺ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商家用户ID（一个用户一个店铺） */
    private Long userId;

    /** 店铺名称 */
    private String shopName;

    /** 主营类目ID */
    private Long categoryId;

    /** 店铺简介 */
    private String description;

    /** 入驻审核状态：PENDING_AUDIT APPROVED REJECTED */
    private String auditStatus;

    /** 管理员审核意见/驳回原因（留痕） */
    private String auditReason;

    /** 店铺营业状态：NORMAL CLOSED */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 软删除：0未删除 1已删除 */
    private Integer deletedFlag;
}
