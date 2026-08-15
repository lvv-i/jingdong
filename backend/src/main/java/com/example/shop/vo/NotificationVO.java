package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 站内通知（T5 U-022：{id, title, content, readStatus, createdAt}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationVO {

    private Long id;

    private String title;

    private String content;

    /** 已读状态：0未读 1已读 */
    private Integer readStatus;

    private LocalDateTime createdAt;
}
