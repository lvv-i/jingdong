package com.example.shop.controller.merchant;

import com.example.shop.common.ApiResult;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.dto.ShipDTO;
import com.example.shop.service.MerchantService;
import com.example.shop.vo.MerchantOrderVO;
import com.example.shop.vo.OrderDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家订单控制器（M-009~M-011；仅 MERCHANT，数据范围：仅本店，T4）
 * M-011 发货：PAID → SHIPPED；写 audit_logs(SHIP)
 */
@RestController
@RequestMapping("/api/merchant/orders")
@RequiredArgsConstructor
public class MerchantOrderController {

    private final MerchantService merchantService;

    /** M-009 订单列表（仅本店；userName 脱敏） */
    @GetMapping
    public ApiResult<PageResult<MerchantOrderVO>> list(PageQuery pageQuery,
                                                       @RequestParam(required = false) String status) {
        return ApiResult.success(merchantService.listOrders(pageQuery, status));
    }

    /** M-010 订单详情（归属校验 6004） */
    @GetMapping("/{id}")
    public ApiResult<OrderDetailVO> detail(@PathVariable Long id) {
        return ApiResult.success(merchantService.orderDetail(id));
    }

    /** M-011 发货（运单号必填 4009） */
    @PostMapping("/{id}/ship")
    public ApiResult<Void> ship(@PathVariable Long id, @Valid @RequestBody ShipDTO dto) {
        merchantService.shipOrder(id, dto);
        return ApiResult.success();
    }
}
