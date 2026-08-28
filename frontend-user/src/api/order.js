// 订单接口（U-012~U-024；W3 提供 U-012/014/015/016，W4 补齐 U-013/017/024）
import request from './request'

/**
 * U-012 创建订单（按商家拆单）
 * @param {Object} data {addressId, cartItemIds:[...], remark?}
 * @returns data.orders: [{orderId, orderNo, status, payAmount}]
 */
export function createOrder(data, config) {
  return request.post('/orders', data, config)
}

/** U-014 订单详情（校验归属本人 4001） */
export function getOrderDetail(id, config) {
  return request.get(`/orders/${id}`, config)
}

/**
 * U-013 订单列表（仅本人）
 * @param {Object} params {page, pageSize, status?}（status 用 T1 状态值，空=全部）
 * @returns data.list: OrderListItemVO {id, orderNo, payAmount, status, createdAt, itemsCount, firstItemImage}
 */
export function listOrders(params, config) {
  return request.get('/orders', { params, ...config })
}

/** U-015 模拟支付：PENDING_PAY → PAID（幂等防重 4008）；返回 data.paymentNo */
export function payOrder(id, config) {
  return request.post(`/orders/${id}/pay`, null, config)
}

/** U-016 取消订单：PENDING_PAY → CANCELLED（回补库存） */
export function cancelOrder(id, config) {
  return request.post(`/orders/${id}/cancel`, null, config)
}

/** U-017 确认收货：SHIPPED → COMPLETED */
export function confirmReceipt(id, config) {
  return request.post(`/orders/${id}/confirm`, null, config)
}

/**
 * U-024 发表评价（仅 COMPLETED 且该明细未评价）
 * @param {Number} orderId 订单ID
 * @param {Object} data {orderItemId, rating 1-5, comment ≤200字}
 */
export function createReview(orderId, data, config) {
  return request.post(`/orders/${orderId}/review`, data, config)
}
