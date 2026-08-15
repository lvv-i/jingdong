package com.example.shop.controller.merchant;

import com.example.shop.common.ApiResult;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.dto.RefundHandleDTO;
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
 * 商家售后控制器（M-012~M-014；仅 MERCHANT，数据范围：仅本店，T4）
 * M-013 同意：REFUNDING → MERCHANT_AGREED；M-014 拒绝：REFUNDING → MERCHANT_REJECTED；均写 audit_logs
 */
@RestController
@RequestMapping("/api/merchant/refunds")
@RequiredArgsConstructor
public class MerchantRefundController {

    private final RefundService refundService;

    /** M-012 商家售后列表（仅本店） */
    @GetMapping
    public ApiResult<PageResult<RefundListItemVO>> list(PageQuery pageQuery,
                                                        @RequestParam(required = false) String status) {
        return ApiResult.success(refundService.listMerchant(pageQuery, status));
    }

    /** M-013 同意退款（reply 持久化 merchant_reply） */
    @PostMapping("/{id}/agree")
    public ApiResult<Void> agree(@PathVariable Long id, @Valid @RequestBody RefundHandleDTO dto) {
        refundService.agree(id, dto.getReply());
        return ApiResult.success();
    }

    /** M-014 拒绝退款（reply 持久化 merchant_reply） */
    @PostMapping("/{id}/reject")
    public ApiResult<Void> reject(@PathVariable Long id, @Valid @RequestBody RefundHandleDTO dto) {
        refundService.reject(id, dto.getReply());
        return ApiResult.success();
    }
}
