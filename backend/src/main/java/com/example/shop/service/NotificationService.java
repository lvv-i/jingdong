package com.example.shop.service;

import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.vo.NotificationVO;

/**
 * 站内通知服务
 * 接口映射：U-022 通知列表 / U-023 标记已读 / U-025 全部已读
 * 数据范围：仅接收者本人（T4 数据权限清单）
 */
public interface NotificationService {

    /** U-022 通知列表（readStatus 过滤：0未读/1已读，可选） */
    PageResult<NotificationVO> list(PageQuery pageQuery, Integer readStatus);

    /** U-023 标记单条已读（校验接收者本人；1004 资源不存在） */
    void markRead(Long id);

    /** U-025 本人全部通知置已读（B 增补） */
    void markAllRead();
}
