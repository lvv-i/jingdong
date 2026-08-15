/**
 * 管理员组接口封装（T5 管理员组 A-001~A-019，前缀 /api/admin/，仅 ADMIN）
 * 函数名 = T5 编号，注释标注编号与 URL
 */
import request from '../utils/request'

// A-001 GET /api/admin/merchants（商家列表，按 auditStatus 筛选）
export function A001_listMerchants(params) {
  return request.get('/admin/merchants', { params })
}

// A-002 POST /api/admin/merchants/{id}/approve（入驻审核通过：PENDING_AUDIT → APPROVED）
export function A002_approveMerchant(id, data) {
  return request.post(`/admin/merchants/${id}/approve`, data)
}

// A-003 POST /api/admin/merchants/{id}/reject（入驻审核驳回：PENDING_AUDIT → REJECTED，auditReason 必填）
export function A003_rejectMerchant(id, data) {
  return request.post(`/admin/merchants/${id}/reject`, data)
}

// A-004 GET /api/admin/categories（类目列表，含禁用）
export function A004_listCategories() {
  return request.get('/admin/categories')
}

// A-005 POST /api/admin/categories（新增类目）
export function A005_createCategory(data) {
  return request.post('/admin/categories', data)
}

// A-006 PUT /api/admin/categories/{id}（编辑类目/启停用）
export function A006_updateCategory(id, data) {
  return request.put(`/admin/categories/${id}`, data)
}

// A-007 DELETE /api/admin/categories/{id}（软删除；有商品时后端拒绝）
export function A007_deleteCategory(id) {
  return request.delete(`/admin/categories/${id}`)
}

// A-008 GET /api/admin/products（全局商品列表，分页）
export function A008_listProducts(params) {
  return request.get('/admin/products', { params })
}

// A-009 POST /api/admin/products/{id}/take-down（巡检强制下架：ON_SALE → OFF_SALE，auditReason 必填）
export function A009_takeDownProduct(id, data) {
  return request.post(`/admin/products/${id}/take-down`, data)
}

// A-010 GET /api/admin/product-audits（待上架审核商品列表）
export function A010_listProductAudits(params) {
  return request.get('/admin/product-audits', { params })
}

// A-011 POST /api/admin/products/{id}/approve（上架审核通过：PENDING_ON_SALE → ON_SALE）
export function A011_approveProduct(id, data) {
  return request.post(`/admin/products/${id}/approve`, data)
}

// A-012 POST /api/admin/products/{id}/reject（上架审核驳回：PENDING_ON_SALE → DRAFT，auditReason 必填）
export function A012_rejectProduct(id, data) {
  return request.post(`/admin/products/${id}/reject`, data)
}

// A-013 GET /api/admin/orders（全局订单列表，支持 orderNo/merchantId 筛选）
export function A013_listOrders(params) {
  return request.get('/admin/orders', { params })
}

// A-014 GET /api/admin/refunds（全局售后列表，分页）
export function A014_listRefunds(params) {
  return request.get('/admin/refunds', { params })
}

// A-015 POST /api/admin/refunds/{id}/handle（退款裁决：ADMIN_INTERVENED → REFUNDED/CLOSED）
export function A015_handleRefund(id, data) {
  return request.post(`/admin/refunds/${id}/handle`, data)
}

// A-016 GET /api/admin/users（用户列表，keyword/role 筛选）
export function A016_listUsers(params) {
  return request.get('/admin/users', { params })
}

// A-017 PUT /api/admin/users/{id}/status（禁用/启用用户）
export function A017_updateUserStatus(id, data) {
  return request.put(`/admin/users/${id}/status`, data)
}

// A-018 GET /api/admin/logs（审计日志查询，分页）
export function A018_listLogs(params) {
  return request.get('/admin/logs', { params })
}

// A-019 GET /api/admin/stats（全局统计）
export function A019_getStats() {
  return request.get('/admin/stats')
}
