// 格式化工具（纯函数，不依赖业务状态）

/** 分（后端金额单位）→ 元，保留 2 位小数 */
export function fenToYuan(fen) {
  if (fen === null || fen === undefined || fen === '') return '0.00'
  return (Number(fen) / 100).toFixed(2)
}

/** 元 → 分（前端传给后端的金额单位） */
export function yuanToFen(yuan) {
  return Math.round(Number(yuan) * 100)
}

/** 日期格式化：后端 yyyy-MM-dd HH:mm:ss 直接展示；空值兜底 */
export function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ')
}
