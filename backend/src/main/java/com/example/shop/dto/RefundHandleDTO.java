package com.example.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家处理退款请求（T5 M-013/M-014：{reply}）
 * reply 持久化 merchant_reply（留痕）；5007：reply 必填
 */
@Data
public class RefundHandleDTO {

    @NotBlank(message = "处理意见不能为空")
    @Size(max = 500, message = "处理意见过长")
    private String reply;
}
