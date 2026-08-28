// 状态映射（只读 T1 状态机枚举表，禁止自造状态名）
// 依据：docs/phase1/member-a/deliverables/01-状态机枚举表.md
// 前端状态展示的唯一来源；按钮显隐由后端返回 status 驱动，不写死

export const ORDER_STATUS_MAP = {
  PENDING_PAY: { label: '待支付', type: 'warning' },
  PAID: { label: '已支付待发货', type: 'primary' },
  SHIPPED: { label: '已发货待收货', type: 'info' },
  COMPLETED: { label: '已完成', type: 'success' },
  CANCELLED: { label: '已取消', type: 'danger' }
}

export const REFUND_STATUS_MAP = {
  REFUNDING: { label: '退款中', type: 'warning' },
  MERCHANT_AGREED: { label: '商家已同意', type: 'primary' },
  MERCHANT_REJECTED: { label: '商家已拒绝', type: 'danger' },
  ADMIN_INTERVENED: { label: '平台介入中', type: 'warning' },
  REFUNDED: { label: '已退款', type: 'success' },
  CLOSED: { label: '已关闭', type: 'info' }
}

/** 订单状态 → {label, type} */
export function orderStatus(status) {
  return ORDER_STATUS_MAP[status] || { label: status || '-', type: 'info' }
}

/** 售后状态 → {label, type} */
export function refundStatus(status) {
  return REFUND_STATUS_MAP[status] || { label: status || '-', type: 'info' }
}

/** 订单中心 Tab（T1 五态 + 全部） */
export const ORDER_TABS = [
  { label: '全部', value: '' },
  { label: '待支付', value: 'PENDING_PAY' },
  { label: '待发货', value: 'PAID' },
  { label: '待收货', value: 'SHIPPED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

/** 售后中心 Tab（T1 退款六态 + 全部） */
export const REFUND_TABS = [
  { label: '全部', value: '' },
  { label: '退款中', value: 'REFUNDING' },
  { label: '商家已同意', value: 'MERCHANT_AGREED' },
  { label: '商家已拒绝', value: 'MERCHANT_REJECTED' },
  { label: '平台介入中', value: 'ADMIN_INTERVENED' },
  { label: '已退款', value: 'REFUNDED' },
  { label: '已关闭', value: 'CLOSED' }
]
