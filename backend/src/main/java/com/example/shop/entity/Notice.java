package com.example.shop.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内通知表实体（数据字典 2.12 notices，read_status 表达业务状态，省略 status）
 * read_status：0未读 1已读
 */
@Data
@TableName("notices")
public class Notice {

    /** 通知ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收者用户ID */
    private Long receiverId;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 已读状态：0未读 1已读 */
    private Integer readStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 软删除：0未删除 1已删除 */
    private Integer deletedFlag;
}
