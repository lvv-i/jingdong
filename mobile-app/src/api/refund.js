/**
 * 售后/退款接口模块（T5 U-018~U-021）
 * 每个函数注释标注 T5 编号、状态流转与主要错误码
 */
import { get, post } from "../utils/request";

/**
 * U-018 发起退款
 * @param {Object} data {orderId, reason, refundAmount}
 * @returns {Promise<{refundId, refundNo, status:REFUNDING}>}
 * 错误码：4001 订单不存在 / 5002 订单状态不可退款 / 5003 已有进行中售后 /
 *         5004 退款金额超实付 / 5006 售后申请失败
 * 订单 status∈{PAID,SHIPPED,COMPLETED} 可发起
 */
export const createRefund = (data) => post("/api/refunds", data);

/**
 * U-019 退款列表
 * @param {Object} params {page, pageSize, status} status 可选
 * @returns {Promise<{list:[{id,refundNo,orderId,refundAmount,reason,status,merchantReply,adminResult,createdAt}], total}>}
 */
export const getRefunds = (params, silent) => get("/api/refunds", params, silent);

/**
 * U-020 撤销退款
 * REFUNDING → CLOSED（商家未处理时）
 * 错误码：5001 退款单不存在 / 5005 状态不允许撤销
 */
export const cancelRefund = (id) => post(`/api/refunds/${id}/cancel`);

/**
 * U-021 申请平台介入
 * MERCHANT_REJECTED → ADMIN_INTERVENED
 * 错误码：5001 / 5005
 */
export const interveneRefund = (id) => post(`/api/refunds/${id}/intervene`);