// 收货地址接口（U-003~U-007，需登录；W3 提供 U-003 供结算页，W5 补齐 U-004~007）
import request from './request'

/** U-003 地址列表（T5 契约：data 为 {list, total}） */
export function getAddresses(config) {
  return request.get('/addresses', config)
}

/**
 * U-004 新增地址（超 20 个返回 2006）
 * @param {Object} data {receiver, phone, province, city, district?, detail, isDefault?}
 * @returns data.id
 */
export function addAddress(data, config) {
  return request.post('/addresses', data, config)
}

/** U-005 编辑地址（校验归属本人 2005） */
export function updateAddress(id, data, config) {
  return request.put(`/addresses/${id}`, data, config)
}

/** U-006 软删除 */
export function deleteAddress(id, config) {
  return request.delete(`/addresses/${id}`, config)
}

/** U-007 设为默认地址 */
export function setDefaultAddress(id, config) {
  return request.put(`/addresses/${id}/default`, null, config)
}
