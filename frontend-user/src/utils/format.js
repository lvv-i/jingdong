// 格式化工具（纯函数，不依赖业务状态）
// 金额单位约定：后端 DECIMAL(12,2)，单位为【元】（数据字典 products.price / order_items.price_snapshot）

/** 金额格式化：元 → "2999.00"（保留两位，空值兜底） */
export function formatPrice(value) {
  if (value === null || value === undefined || value === '') return '0.00'
  return Number(value).toFixed(2)
}

/** 日期格式化：后端 yyyy-MM-dd HH:mm:ss 直接展示；空值兜底 */
export function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ')
}
