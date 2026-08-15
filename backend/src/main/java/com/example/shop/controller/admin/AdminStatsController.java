package com.example.shop.controller.admin;

import com.example.shop.common.ApiResult;
import com.example.shop.service.StatsService;
import com.example.shop.vo.AdminStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员统计控制器（A-019 平台仪表盘；仅 ADMIN）
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminStatsController {

    private final StatsService statsService;

    /** A-019 平台仪表盘统计 */
    @GetMapping("/stats")
    public ApiResult<AdminStatsVO> stats() {
        return ApiResult.success(statsService.adminStats());
    }
}
