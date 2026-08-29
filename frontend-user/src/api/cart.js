// 购物车接口（U-008~U-011，需登录）
import request from './request'

/** U-008 购物车列表（T5 契约：data 为 {list, total}；当前价实时读取） */
export function getCartItems(config) {
  return request.get('/cart/items', config)
}

/** U-009 加入购物车（仅 ON_SALE 可加购；重复商品累加数量；返回 data.id） */
export function addCartItem(data, config) {
  return request.post('/cart/items', data, config)
}

/** U-010 修改数量/勾选（{quantity?, selected?}） */
export function updateCartItem(id, data, config) {
  return request.put(`/cart/items/${id}`, data, config)
}

/** U-011 软删除 */
export function deleteCartItem(id, config) {
  return request.delete(`/cart/items/${id}`, config)
}
