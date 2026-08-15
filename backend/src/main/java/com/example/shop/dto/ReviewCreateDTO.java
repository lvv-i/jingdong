package com.example.shop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评价请求（T5 U-024：{orderItemId, rating(1-5), comment(≤200字)}）
 * 仅订单 COMPLETED 且该明细未评价（T5 决议 #2）；写 order_items.rating/comment/reviewed_at
 */
@Data
public class ReviewCreateDTO {

    @NotNull(message = "订单明细ID不能为空")
    private Long orderItemId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分范围为 1-5")
    @Max(value = 5, message = "评分范围为 1-5")
    private Integer rating;

    @NotBlank(message = "评价内容不能为空")
    @Size(max = 200, message = "评价内容不能超过 200 字")
    private String comment;
}
