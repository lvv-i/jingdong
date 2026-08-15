package com.example.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员审核请求（T5 A-002/A-003/A-009/A-011/A-012：{auditReason}）
 * 审核意见/驳回原因/下架原因必填并留痕（7002）；写 audit_logs
 */
@Data
public class AuditReasonDTO {

    @NotBlank(message = "审核意见不能为空")
    @Size(max = 500, message = "审核意见过长")
    private String auditReason;
}
