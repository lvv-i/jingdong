package com.example.shop.controller;

import com.example.shop.common.ApiResult;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.dto.OrderCreateDTO;
import com.example.shop.dto.ReviewCreateDTO;
import com.example.shop.service.OrderService;
import com.example.shop.vo.OrderCreateVO;
import com.example.shop.vo.OrderDetailVO;
import com.example.shop.vo.OrderListItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户订单控制器
 * U-012 创建 / U-013 列表 / U-014 详情 / U-015 支付 / U-016 取消 / U-017 确认收货 / U-024 评价
 * 数据范围：仅本人（T4 数据权限清单 #1）
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** U-012 创建订单（按商家拆单；扣库存；写快照明细） */
    @PostMapping
    public ApiResult<OrderCreateVO> create(@Valid @RequestBody OrderCreateDTO dto) {
        return ApiResult.success(orderService.create(dto));
    }

    /** U-013 订单列表（status 过滤用 T1 订单状态值） */
    @GetMapping
    public ApiResult<PageResult<OrderListItemVO>> list(PageQuery pageQuery,
                                                       @RequestParam(required = false) String status) {
        return ApiResult.success(orderService.list(pageQuery, status));
    }

    /** U-014 订单详情（校验归属本人 4001） */
    @GetMapping("/{id}")
    public ApiResult<OrderDetailVO> detail(@PathVariable Long id) {
        return ApiResult.success(orderService.detail(id));
    }

    /** U-015 模拟支付：PENDING_PAY → PAID（幂等防重 4008） */
    @PostMapping("/{id}/pay")
    public ApiResult<Map<String, String>> pay(@PathVariable Long id) {
        return ApiResult.success(Map.of("paymentNo", orderService.pay(id)));
    }

    /** U-016 取消订单：PENDING_PAY → CANCELLED（回补库存） */
    @PostMapping("/{id}/cancel")
    public ApiResult<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return ApiResult.success();
    }

    /** U-017 确认收货：SHIPPED → COMPLETED */
    @PostMapping("/{id}/confirm")
    public ApiResult<Void> confirmReceipt(@PathVariable Long id) {
        orderService.confirmReceipt(id);
        return ApiResult.success();
    }

    /** U-024 发表评价（仅 COMPLETED 且该明细未评价） */
    @PostMapping("/{id}/review")
    public ApiResult<Void> review(@PathVariable Long id, @Valid @RequestBody ReviewCreateDTO dto) {
        orderService.review(id, dto);
        return ApiResult.success();
    }
}
