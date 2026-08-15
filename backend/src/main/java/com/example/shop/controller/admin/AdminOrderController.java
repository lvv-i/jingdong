package com.example.shop.controller.admin;

import com.example.shop.common.ApiResult;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.service.AdminService;
import com.example.shop.vo.AdminOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员订单控制器（A-013 全局订单列表；仅 ADMIN）
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminService adminService;

    /** A-013 全局订单列表（status/orderNo/merchantId 过滤；含 userName/merchantName） */
    @GetMapping
    public ApiResult<PageResult<AdminOrderVO>> list(PageQuery pageQuery,
                                                    @RequestParam(required = false) String status,
                                                    @RequestParam(required = false) String orderNo,
                                                    @RequestParam(required = false) Long merchantId) {
        return ApiResult.success(adminService.listOrders(pageQuery, status, orderNo, merchantId));
    }
}
