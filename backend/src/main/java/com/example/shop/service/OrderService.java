package com.example.shop.service;

import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.dto.OrderCreateDTO;
import com.example.shop.dto.ReviewCreateDTO;
import com.example.shop.vo.OrderCreateVO;
import com.example.shop.vo.OrderDetailVO;
import com.example.shop.vo.OrderListItemVO;

/**
 * 用户订单服务
 * 接口映射：U-012 创建 / U-013 列表 / U-014 详情 / U-015 模拟支付 / U-016 取消 / U-017 确认收货 / U-024 发表评价
 * 状态流转（T1 订单状态机，Service 层强制校验）：
 *   PENDING_PAY → PAID（支付）/ CANCELLED（用户取消或超时）；PAID → SHIPPED（商家，见 MerchantService）；
 *   SHIPPED → COMPLETED（确认收货）
 */
public interface OrderService {

    /** U-012 创建订单（按商家拆单；校验库存与价格快照；扣库存；写快照明细；移除购物车项） */
    OrderCreateVO create(OrderCreateDTO dto);

    /** U-013 订单列表（status 过滤用 T1 订单状态值；firstItemImage 首商品主图） */
    PageResult<OrderListItemVO> list(PageQuery pageQuery, String status);

    /** U-014 订单详情（校验归属本人 4001） */
    OrderDetailVO detail(Long id);

    /** U-015 模拟支付：PENDING_PAY → PAID；写 payment_records(PAY)；幂等防重 4008 */
    String pay(Long id);

    /** U-016 取消订单：PENDING_PAY → CANCELLED；回补库存 */
    void cancel(Long id);

    /** U-017 确认收货：SHIPPED → COMPLETED */
    void confirmReceipt(Long id);

    /** U-024 发表评价（仅 COMPLETED 且该明细未评价；写 order_items.rating/comment/reviewed_at） */
    void review(Long orderId, ReviewCreateDTO dto);
}
