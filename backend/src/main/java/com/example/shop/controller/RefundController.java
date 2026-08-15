package com.example.shop.controller;

import com.example.shop.common.ApiResult;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.dto.RefundCreateDTO;
import com.example.shop.service.RefundService;
import com.example.shop.vo.RefundListItemVO;
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
 * 用户售后控制器（U-018~U-021；数据范围：仅本人，T4）
 * 流转：U-018 发起 / U-020 撤销 / U-021 申请平台介入
 */
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    /** U-018 发起退款（订单 status∈{PAID,SHIPPED,COMPLETED}；无进行中售后单；金额≤实付） */
    @PostMapping
    public ApiResult<Map<String, Object>> create(@Valid @RequestBody RefundCreateDTO dto) {
        Long id = refundService.create(dto);
        return ApiResult.success(java.util.Map.of("refundId", id, "status", "REFUNDING"));
    }

    /** U-019 用户售后列表（仅本人） */
    @GetMapping
    public ApiResult<PageResult<RefundListItemVO>> list(PageQuery pageQuery,
                                                        @RequestParam(required = false) String status) {
        return ApiResult.success(refundService.listUser(pageQuery, status));
    }

    /** U-020 撤销退款：REFUNDING → CLOSED（商家未处理时） */
    @PostMapping("/{id}/cancel")
    public ApiResult<Void> cancel(@PathVariable Long id) {
        refundService.cancel(id);
        return ApiResult.success();
    }

    /** U-021 申请平台介入：MERCHANT_REJECTED → ADMIN_INTERVENED */
    @PostMapping("/{id}/intervene")
    public ApiResult<Void> intervene(@PathVariable Long id) {
        refundService.intervene(id);
        return ApiResult.success();
    }
}
