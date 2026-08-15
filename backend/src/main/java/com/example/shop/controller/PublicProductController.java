package com.example.shop.controller;

import com.example.shop.common.ApiResult;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.service.CategoryService;
import com.example.shop.service.ProductQueryService;
import com.example.shop.vo.CategoryVO;
import com.example.shop.vo.ProductDetailVO;
import com.example.shop.vo.ProductListItemVO;
import com.example.shop.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开浏览控制器（白名单，无需登录）
 * P-003 类目树 / P-004 商品列表 / P-005 商品详情 / P-006 商品评价列表
 */
@RestController
@RequiredArgsConstructor
public class PublicProductController {

    private final CategoryService categoryService;
    private final ProductQueryService productQueryService;

    /** P-003 类目树（仅 ENABLED；data 为 {list, total}） */
    @GetMapping("/api/categories")
    public ApiResult<PageResult<CategoryVO>> categories() {
        List<CategoryVO> list = categoryService.listEnabled();
        return ApiResult.success(PageResult.of(list, list.size()));
    }

    /** P-004 商品列表（仅 ON_SALE；sort：综合(默认)/sales/priceAsc/priceDesc） */
    @GetMapping("/api/products")
    public ApiResult<PageResult<ProductListItemVO>> products(PageQuery pageQuery,
                                                             @RequestParam(required = false) Long categoryId,
                                                             @RequestParam(required = false) String keyword,
                                                             @RequestParam(required = false) Long merchantId,
                                                             @RequestParam(required = false) String sort) {
        return ApiResult.success(productQueryService.list(pageQuery, categoryId, keyword, merchantId, sort));
    }

    /** P-005 商品详情（非 ON_SALE 不可见 → 3002） */
    @GetMapping("/api/products/{id}")
    public ApiResult<ProductDetailVO> productDetail(@PathVariable Long id) {
        return ApiResult.success(productQueryService.detail(id));
    }

    /** P-006 商品评价列表（仅已评价明细；userName 脱敏） */
    @GetMapping("/api/products/{id}/reviews")
    public ApiResult<PageResult<ReviewVO>> productReviews(@PathVariable Long id, PageQuery pageQuery) {
        return ApiResult.success(productQueryService.listReviews(id, pageQuery));
    }
}
