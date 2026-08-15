package com.example.shop.controller;

import com.example.shop.common.ApiResult;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.service.NotificationService;
import com.example.shop.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站内通知控制器（U-022/U-023/U-025；数据范围：仅接收者本人，T4）
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** U-022 通知列表（readStatus 过滤：0未读/1已读） */
    @GetMapping
    public ApiResult<PageResult<NotificationVO>> list(PageQuery pageQuery,
                                                      @RequestParam(required = false) Integer readStatus) {
        return ApiResult.success(notificationService.list(pageQuery, readStatus));
    }

    /** U-023 标记单条已读（校验接收者本人） */
    @PutMapping("/{id}/read")
    public ApiResult<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ApiResult.success();
    }

    /** U-025 本人全部通知置已读（B 增补） */
    @PutMapping("/read-all")
    public ApiResult<Void> markAllRead() {
        notificationService.markAllRead();
        return ApiResult.success();
    }
}
