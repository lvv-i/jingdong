package com.example.shop.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家重新提交入驻审核请求（T5 M-002b，T1 3.2"修改资料后重新提交"）
 * 全部字段可选：不携带则按现有资料重提；携带的字段先更新再置 PENDING_AUDIT
 */
@Data
public class ShopResubmitDTO {

    /** 店铺名称（可选） */
    @Size(max = 100, message = "店铺名称过长")
    private String shopName;

    /** 主营类目（可选，携带时校验存在 3004） */
    private Long categoryId;

    /** 店铺简介（可选） */
    @Size(max = 500, message = "店铺简介过长")
    private String description;
}
