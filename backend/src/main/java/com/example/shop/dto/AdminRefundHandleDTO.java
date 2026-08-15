package com.example.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员退款裁决请求（T5 A-015：{adminResult, agree}）
 * ADMIN_INTERVENED → REFUNDED（agree=true）/ CLOSED（agree=false）；写 audit_logs(HANDLE_REFUND)
 */
@Data
public class AdminRefundHandleDTO {

    @NotBlank(message = "裁决意见不能为空")
    @Size(max = 500, message = "裁决意见过长")
    private String adminResult;

    /** true=同意退款（REFUNDED）；false=驳回（CLOSED） */
    @NotNull(message = "agree 不能为空")
    private Boolean agree;
}
