package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收货地址（T5 U-003：{id, receiver, phone, province, city, district, detail, isDefault}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressVO {

    private Long id;

    private String receiver;

    private String phone;

    private String province;

    private String city;

    private String district;

    private String detail;

    /** 是否默认地址：0否 1是 */
    private Integer isDefault;
}
