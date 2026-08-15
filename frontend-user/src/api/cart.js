// 购物车接口（U-008~U-011，需登录；W2 先提供 U-009 供详情页加购，其余 W3 补齐）
import request from './request'

/** U-009 加入购物车（仅 ON_SALE 可加购；重复商品累加数量；返回 data.id） */
export function addCartItem(data, config) {
  return request.post('/cart/items', data, config)
}
