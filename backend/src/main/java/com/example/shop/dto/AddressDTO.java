package com.example.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 收货地址请求（T5 U-004/U-005：{receiver, phone, province, city, district, detail, isDefault}）
 */
@Data
public class AddressDTO {

    @NotBlank(message = "收货人不能为空")
    @Size(max = 50, message = "收货人姓名过长")
    private String receiver;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String phone;

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "城市不能为空")
    private String city;

    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detail;

    /** 是否默认地址：0否 1是 */
    private Integer isDefault;
}
