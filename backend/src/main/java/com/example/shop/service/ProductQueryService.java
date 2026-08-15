package com.example.shop.service;

import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.vo.ProductDetailVO;
import com.example.shop.vo.ProductListItemVO;
import com.example.shop.vo.ReviewVO;

/**
 * 公开商品查询服务
 * 接口映射：P-004 商品列表 / P-005 商品详情 / P-006 评价列表
 */
public interface ProductQueryService {

    /**
     * P-004 商品列表（仅 ON_SALE）
     * @param sort 排序：综合(默认)/sales/priceAsc/priceDesc（B 增补）
     */
    PageResult<ProductListItemVO> list(PageQuery page, Long categoryId, String keyword, Long merchantId, String sort);

    /** P-005 商品详情（非 ON_SALE 不可见 → 3002） */
    ProductDetailVO detail(Long id);

    /** P-006 评价列表（仅已评价明细；userName 脱敏） */
    PageResult<ReviewVO> listReviews(Long productId, PageQuery page);
}
