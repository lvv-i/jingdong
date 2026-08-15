/**
 * 状态映射唯一来源（.qoder/members/member-c/rules/admin-frontend.md 第 1 节）
 * 状态值一律引用 T1 状态机枚举（docs/phase1/member-a/deliverables/01-状态机枚举表.md），
 * 前端只读，禁止自行发明状态名；页面禁止散落硬编码。
 */

// 订单状态（T1：5 态）
export const ORDER_STATUS = {
  PENDING_PAY: { label: '待支付', tag: 'warning' },
  PAID: { label: '已支付待发货', tag: 'primary' },
  SHIPPED: { label: '已发货待收货', tag: 'info' },
  COMPLETED: { label: '已完成', tag: 'success' },
  CANCELLED: { label: '已取消', tag: 'danger' }
}

// 售后单状态（T1：6 态）
export const REFUND_STATUS = {
  REFUNDING: { label: '退款中', tag: 'warning' },
  MERCHANT_AGREED: { label: '商家同意', tag: 'success' },
  MERCHANT_REJECTED: { label: '商家拒绝', tag: 'danger' },
  ADMIN_INTERVENED: { label: '平台介入', tag: 'primary' },
  REFUNDED: { label: '已退款', tag: 'success' },
  CLOSED: { label: '已关闭', tag: 'info' }
}

// 商家入驻状态（T1：3 态）
export const MERCHANT_AUDIT_STATUS = {
  PENDING_AUDIT: { label: '待审核', tag: 'warning' },
  APPROVED: { label: '已通过', tag: 'success' },
  REJECTED: { label: '已驳回', tag: 'danger' }
}

// 商品状态（T1：4 态）
export const PRODUCT_STATUS = {
  DRAFT: { label: '草稿', tag: 'info' },
  PENDING_ON_SALE: { label: '待上架', tag: 'warning' },
  ON_SALE: { label: '已上架', tag: 'success' },
  OFF_SALE: { label: '已下架', tag: 'danger' }
}

// 通用：按状态取值取配置，未知值原样展示（不发明新状态名）
export function statusInfo(map, value) {
  return map[value] || { label: value, tag: 'info' }
}
