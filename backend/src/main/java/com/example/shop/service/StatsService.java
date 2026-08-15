package com.example.shop.service;

import com.example.shop.vo.AdminStatsVO;
import com.example.shop.vo.MerchantStatsVO;

/**
 * 统计服务
 * 接口映射：M-015 店铺仪表盘（C 增补）/ A-019 平台仪表盘
 */
public interface StatsService {

    /** M-015 店铺仪表盘统计（仅本店；6001 无店铺） */
    MerchantStatsVO merchantStats();

    /** A-019 平台仪表盘统计（全局） */
    AdminStatsVO adminStats();
}
