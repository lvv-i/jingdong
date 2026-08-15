// 商品浏览接口（P-003~P-006，公开白名单，无需登录）
import request from './request'

/** P-003 类目树（仅 ENABLED；返回 data.list） */
export function getCategories(config) {
  return request.get('/categories', config)
}

/**
 * P-004 商品列表（仅 ON_SALE）
 * @param {Object} params {page, pageSize, categoryId, keyword, merchantId, sort}
 * sort：默认综合 / sales 销量 / priceAsc 价格升 / priceDesc 价格降
 */
export function getProducts(params, config) {
  return request.get('/products', { params, ...config })
}

/** P-005 商品详情（非 ON_SALE → 3002） */
export function getProductDetail(id, config) {
  return request.get(`/products/${id}`, config)
}

/** P-006 商品评价列表（userName 已脱敏） */
export function getProductReviews(id, params, config) {
  return request.get(`/products/${id}/reviews`, { params, ...config })
}
