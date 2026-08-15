package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 商品评价（T5 P-006：{userName(脱敏), rating, comment, reviewedAt}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewVO {

    /** 评价用户名（脱敏） */
    private String userName;

    /** 评分 1-5 */
    private Integer rating;

    /** 评价内容 */
    private String comment;

    /** 评价时间 */
    private LocalDateTime reviewedAt;
}
