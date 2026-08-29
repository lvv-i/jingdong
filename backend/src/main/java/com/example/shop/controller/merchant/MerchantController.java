package com.example.shop.controller.merchant;

import com.example.shop.common.ApiResult;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.dto.ProductSaveDTO;
import com.example.shop.dto.ShopUpdateDTO;
import com.example.shop.dto.StockUpdateDTO;
import com.example.shop.service.MerchantService;
import com.example.shop.vo.MerchantProductVO;
import com.example.shop.vo.ShopVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 商家店铺与商品控制器（M-001~M-008；仅 MERCHANT，数据范围：仅本店，T4）
 * M-003 创建为草稿 DRAFT；M-006 提交上架；M-007 主动下架；M-008 改库存
 */
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    /** M-001 店铺信息（6001 无店铺） */
    @GetMapping("/shop")
    public ApiResult<ShopVO> shop() {
        return ApiResult.success(merchantService.getShop());
    }

    /** M-002 编辑店铺（非 APPROVED 状态不可编辑） */
    @PutMapping("/shop")
    public ApiResult<Void> updateShop(@Valid @RequestBody ShopUpdateDTO dto) {
        merchantService.updateShop(dto);
        return ApiResult.success();
    }

    /** M-002b 重新提交入驻审核：REJECTED → PENDING_AUDIT（T1 3.2） */
    @PostMapping("/shop/resubmit")
    public ApiResult<Void> resubmitShop() {
        merchantService.resubmitShop();
        return ApiResult.success();
    }

    /** M-003 创建商品（DRAFT；images 写入 product_images） */
    @PostMapping("/products")
    public ApiResult<Map<String, Object>> createProduct(@Valid @RequestBody ProductSaveDTO dto) {
        Long id = merchantService.createProduct(dto);
        return ApiResult.success(Map.of("id", id, "status", "DRAFT"));
    }

    /** M-004 商品列表（仅本店；status/keyword 过滤） */
    @GetMapping("/products")
    public ApiResult<PageResult<MerchantProductVO>> listProducts(PageQuery pageQuery,
                                                                 @RequestParam(required = false) String status,
                                                                 @RequestParam(required = false) String keyword) {
        return ApiResult.success(merchantService.listProducts(pageQuery, status, keyword));
    }

    /** M-005 编辑商品（仅 DRAFT/OFF_SALE 可编辑） */
    @PutMapping("/products/{id}")
    public ApiResult<Void> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductSaveDTO dto) {
        merchantService.updateProduct(id, dto);
        return ApiResult.success();
    }

    /** M-006 提交上架：DRAFT/OFF_SALE → PENDING_ON_SALE */
    @PostMapping("/products/{id}/submit")
    public ApiResult<Void> submitProduct(@PathVariable Long id) {
        merchantService.submitProduct(id);
        return ApiResult.success();
    }

    /** M-007 主动下架：ON_SALE → OFF_SALE */
    @PostMapping("/products/{id}/off")
    public ApiResult<Void> offProduct(@PathVariable Long id) {
        merchantService.offProduct(id);
        return ApiResult.success();
    }

    /** M-008 修改库存（仅本店） */
    @PutMapping("/products/{id}/stock")
    public ApiResult<Void> updateStock(@PathVariable Long id, @Valid @RequestBody StockUpdateDTO dto) {
        merchantService.updateStock(id, dto);
        return ApiResult.success();
    }
}
