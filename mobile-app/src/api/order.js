/**
 * 订单接口模块（T5 U-012~U-017；U-024 评价见 api/order.js 底部）
 */
import { get, post } from "../utils/request";

/**
 * U-012 创建订单（按商家拆单）
 * @param {Object} data {addressId 必填, cartItemIds 非空, remark 可选}
 * @returns {Promise<{orders:[{orderId, orderNo, status, payAmount}]}>}
 * 错误码：4003 购物车项失效 / 4004 购物车为空 / 4005 地址无效 / 4006 库存不足 /
 *         3001 商品不存在 / 3002 商品已下架
 */
export const createOrder = (data) => post("/api/orders", data);

// U-013 订单列表：?status= （空=全部；PENDING_PAY/PAID/SHIPPED/COMPLETED/CANCELLED）
export const getOrders = (params, silent) => get("/api/orders", params, silent);

// U-014 订单详情（含 receiverSnapshot 快照、items、物流时间线）
export const getOrderDetail = (id, silent) => get(`/api/orders/${id}`, null, silent);

/**
 * U-015 模拟支付：仅 PENDING_PAY 可支付，返回 {paymentNo}
 * 错误码：4001 归属 / 4002 状态或超时 / 3001 商品不存在 / 3002 下架 /
 *         3003 库存 / 4007 价格变化 / 4008 已支付幂等
 */
export const payOrder = (id) => post(`/api/orders/${id}/pay`);

// U-016 取消订单：仅 PENDING_PAY 可取消（回补库存）
export const cancelOrder = (id) => post(`/api/orders/${id}/cancel`);

// U-017 确认收货：仅 SHIPPED 可确认 → COMPLETED
export const confirmOrder = (id) => post(`/api/orders/${id}/confirm`);

// U-024 订单评价：POST /api/orders/{id}/review（X4 订单链路使用；错误码 4001/4002/4003）
export const reviewOrder = (id, data) => post(`/api/orders/${id}/review`, data);
