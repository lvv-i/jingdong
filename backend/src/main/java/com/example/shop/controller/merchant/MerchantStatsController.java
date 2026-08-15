package com.example.shop.controller.merchant;

import com.example.shop.common.ApiResult;
import com.example.shop.service.StatsService;
import com.example.shop.vo.MerchantStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家统计控制器（M-015 店铺仪表盘，C 增补；仅 MERCHANT，数据范围：仅本店）
 */
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantStatsController {

    private final StatsService statsService;

    /** M-015 店铺仪表盘统计 */
    @GetMapping("/stats")
    public ApiResult<MerchantStatsVO> stats() {
        return ApiResult.success(statsService.merchantStats());
    }
}
