package com.example.shop.controller.admin;

import com.example.shop.common.ApiResult;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.service.AdminService;
import com.example.shop.vo.AuditLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员审计日志控制器（A-018；仅 ADMIN）
 */
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class AdminLogController {

    private final AdminService adminService;

    /** A-018 审计日志列表（operatorRole/targetType 过滤） */
    @GetMapping
    public ApiResult<PageResult<AuditLogVO>> list(PageQuery pageQuery,
                                                  @RequestParam(required = false) String operatorRole,
                                                  @RequestParam(required = false) String targetType) {
        return ApiResult.success(adminService.listLogs(pageQuery, operatorRole, targetType));
    }
}
