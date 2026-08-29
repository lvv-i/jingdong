/**
 * 购物车接口模块（T5 U-008/U-010/U-011；U-009 加购见 api/product.js）
 */
import { get, put, del } from "../utils/request";

// U-008 购物车列表：后端实际返回裸数组 item[]（T5 文档写 {list,total}，页面已按裸数组消费）
export const getCartItems = (silent) => get("/api/cart/items", null, silent);

/**
 * U-010 修改购物车项
 * @param {number} id 购物车项 id
 * @param {Object} data {quantity 1-999, selected 0/1}
 * 错误码：4003 购物车项不存在/已失效
 */
export const updateCartItem = (id, data) => put(`/api/cart/items/${id}`, data);

// U-011 删除购物车项：错误码 4003
export const deleteCartItem = (id) => del(`/api/cart/items/${id}`);
