package com.example.shop.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表实体（数据字典 2.1 users）
 * role：USER普通用户 MERCHANT商家 ADMIN管理员
 * status：NORMAL正常 DISABLED禁用
 * 软删除 deleted_flag 已全局配置（application.yml logic-delete-field）
 */
@Data
@TableName("users")
public class User {

    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（登录名） */
    private String username;

    /** 密码哈希（BCrypt） */
    private String passwordHash;

    /** 手机号 */
    private String phone;

    /** 角色：USER MERCHANT ADMIN */
    private String role;

    /** 状态：NORMAL DISABLED */
    private String status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 软删除：0未删除 1已删除 */
    private Integer deletedFlag;
}
