// 售后接口（U-018~U-021）
import request from './request'

/**
 * U-018 发起退款（订单 status∈{PAID,SHIPPED,COMPLETED}；5002 状态不允许/5003 重复/5004 金额非法/5006 原因必填）
 * @param {Object} data {orderId, reason, refundAmount}
 * @returns data: {refundId, status: 'REFUNDING'}
 */
export function createRefund(data, config) {
  return request.post('/refunds', data, config)
}

/**
 * U-019 售后列表（仅本人）
 * @param {Object} params {page, pageSize, status?}（六态值，空=全部）
 * @returns data.list: RefundListItemVO {id, refundNo, orderId, refundAmount, reason, status, merchantReply, adminResult, createdAt}
 */
export function getRefunds(params, config) {
  return request.get('/refunds', { params, ...config })
}

/** U-020 撤销退款：REFUNDING → CLOSED（商家未处理时；5001/5005 状态不允许） */
export function cancelRefund(id, config) {
  return request.post(`/refunds/${id}/cancel`, null, config)
}

/** U-021 申请平台介入：MERCHANT_REJECTED → ADMIN_INTERVENED（5001/5005 状态不允许） */
export function interveneRefund(id, config) {
  return request.post(`/refunds/${id}/intervene`, null, config)
}
