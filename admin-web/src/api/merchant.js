/**
 * 商家组接口封装（T5 商家组 M-001~M-015，前缀 /api/merchant/，仅 MERCHANT）
 * 函数名 = T5 编号，注释标注编号与 URL
 */
import request from '../utils/request'

// M-001 GET /api/merchant/shop（店铺资料）
export function M001_getShop() {
  return request.get('/merchant/shop')
}

// M-002 PUT /api/merchant/shop（编辑店铺；非 APPROVED 不可编辑）
export function M002_updateShop(data) {
  return request.put('/merchant/shop', data)
}

// M-003 POST /api/merchant/products（创建商品，初始 DRAFT）
export function M003_createProduct(data) {
  return request.post('/merchant/products', data)
}

// M-004 GET /api/merchant/products（本店商品列表，分页）
export function M004_listProducts(params) {
  return request.get('/merchant/products', { params })
}

// M-005 PUT /api/merchant/products/{id}（编辑商品，仅 DRAFT/OFF_SALE）
export function M005_updateProduct(id, data) {
  return request.put(`/merchant/products/${id}`, data)
}

// M-006 POST /api/merchant/products/{id}/submit（提交上架审核：DRAFT → PENDING_ON_SALE）
export function M006_submitProduct(id) {
  return request.post(`/merchant/products/${id}/submit`)
}

// M-007 POST /api/merchant/products/{id}/off（商家下架：ON_SALE → OFF_SALE）
export function M007_offProduct(id) {
  return request.post(`/merchant/products/${id}/off`)
}

// M-008 PUT /api/merchant/products/{id}/stock（修改库存）
export function M008_updateStock(id, data) {
  return request.put(`/merchant/products/${id}/stock`, data)
}

// M-009 GET /api/merchant/orders（本店订单列表，分页；list 项含 userName 脱敏）
export function M009_listOrders(params) {
  return request.get('/merchant/orders', { params })
}

// M-010 GET /api/merchant/orders/{id}（订单详情）
export function M010_getOrder(id) {
  return request.get(`/merchant/orders/${id}`)
}

// M-011 POST /api/merchant/orders/{id}/ship（发货：PAID → SHIPPED，shippingNo 必填）
export function M011_shipOrder(id, data) {
  return request.post(`/merchant/orders/${id}/ship`, data)
}

// M-012 GET /api/merchant/refunds（本店售后列表，分页）
export function M012_listRefunds(params) {
  return request.get('/merchant/refunds', { params })
}

// M-013 POST /api/merchant/refunds/{id}/agree（同意退款：REFUNDING → MERCHANT_AGREED，reply 必填）
export function M013_agreeRefund(id, data) {
  return request.post(`/merchant/refunds/${id}/agree`, data)
}

// M-014 POST /api/merchant/refunds/{id}/reject（拒绝退款：REFUNDING → MERCHANT_REJECTED，reply 必填）
export function M014_rejectRefund(id, data) {
  return request.post(`/merchant/refunds/${id}/reject`, data)
}

// M-015 GET /api/merchant/stats（店铺仪表盘统计）
export function M015_getStats() {
  return request.get('/merchant/stats')
}
