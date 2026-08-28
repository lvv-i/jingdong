// 收货地址接口（U-003~U-007，需登录；W3 提供 U-003 供结算页，U-004~007 留 W5）
import request from './request'

/** U-003 地址列表 */
export function getAddresses(config) {
  return request.get('/addresses', config)
}
