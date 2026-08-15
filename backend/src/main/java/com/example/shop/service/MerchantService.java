package com.example.shop.service;

import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.dto.ProductSaveDTO;
import com.example.shop.dto.ShipDTO;
import com.example.shop.dto.ShopUpdateDTO;
import com.example.shop.dto.StockUpdateDTO;
import com.example.shop.vo.MerchantOrderVO;
import com.example.shop.vo.MerchantProductVO;
import com.example.shop.vo.OrderDetailVO;
import com.example.shop.vo.ShopVO;

/**
 * 商家服务
 * 接口映射：M-001~M-011（店铺/商品/订单）
 * 数据范围：仅本店（T4 数据权限清单 #2：shopId 来自 JWT，资源归属校验 merchant_id==shopId）
 * 商品状态机（T1）：M-003 创建为 DRAFT；M-006 DRAFT→PENDING_ON_SALE；M-007 ON_SALE→OFF_SALE
 */
public interface MerchantService {

    /** M-001 店铺信息（无店铺 6001） */
    ShopVO getShop();

    /** M-002 编辑店铺（非 APPROVED 状态不可编辑：6002/6003/6005） */
    void updateShop(ShopUpdateDTO dto);

    /** M-003 创建商品（DRAFT；images 写 product_images；店铺须 APPROVED 6002；类目校验 3004） */
    Long createProduct(ProductSaveDTO dto);

    /** M-004 商品列表（仅本店；status/keyword 过滤） */
    PageResult<MerchantProductVO> listProducts(PageQuery pageQuery, String status, String keyword);

    /** M-005 编辑商品（仅 DRAFT/OFF_SALE 可编辑 3005；非本店 6004） */
    void updateProduct(Long id, ProductSaveDTO dto);

    /** M-006 提交上架：DRAFT → PENDING_ON_SALE（信息不完整 3006） */
    void submitProduct(Long id);

    /** M-007 主动下架：ON_SALE → OFF_SALE */
    void offProduct(Long id);

    /** M-008 修改库存（仅本店 6004） */
    void updateStock(Long id, StockUpdateDTO dto);

    /** M-009 订单列表（仅本店；userName 脱敏） */
    PageResult<MerchantOrderVO> listOrders(PageQuery pageQuery, String status);

    /** M-010 订单详情（归属校验 6004；结构同 U-014） */
    OrderDetailVO orderDetail(Long id);

    /** M-011 发货：PAID → SHIPPED（运单号必填 4009；写 audit_logs(SHIP)） */
    void shipOrder(Long id, ShipDTO dto);
}
