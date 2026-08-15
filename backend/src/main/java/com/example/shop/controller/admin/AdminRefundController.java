package com.example.shop.controller.admin;

import com.example.shop.common.ApiResult;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.dto.AdminRefundHandleDTO;
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

/**
 * 管理员售后控制器（A-014/A-015；仅 ADMIN；裁决写 audit_logs，T4）
 * A-015 裁决：ADMIN_INTERVENED → REFUNDED（agree=true）/ CLOSED（agree=false）
 */
@RestController
@RequestMapping("/api/admin/refunds")
@RequiredArgsConstructor
public class AdminRefundController {

    private final RefundService refundService;

    /** A-014 全局退款列表（status 过滤） */
    @GetMapping
    public ApiResult<PageResult<RefundListItemVO>> list(PageQuery pageQuery,
                                                        @RequestParam(required = false) String status) {
        return ApiResult.success(refundService.listAdmin(pageQuery, status));
    }

    /** A-015 退款裁决（agree=true 写 payment_records(REFUND)） */
    @PostMapping("/{id}/handle")
    public ApiResult<Void> handle(@PathVariable Long id, @Valid @RequestBody AdminRefundHandleDTO dto) {
        refundService.handle(id, dto);
        return ApiResult.success();
    }
}
