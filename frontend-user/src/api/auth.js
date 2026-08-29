// 认证接口封装（T5 公共组）
// P-001 注册 / P-002 密码登录 / P-007 短信验证码 / P-008 验证码登录
// U-001 个人资料 / U-002 修改资料
import request from './request'

/** P-001 注册：{username, password, phone} → data:{userId}；错误 1001/2001/2002 */
export function register(data, config) {
  return request.post('/users/register', data, config)
}

/** P-002 账号密码登录：{username, password} → data:{token, userInfo:{id,username,role,shopId}}；错误 1001/2003/2004 */
export function login(data, config) {
  return request.post('/users/login', data, config)
}

/** P-007 发送短信验证码：{phone}；demo 固定码 123456；60 秒限频；错误 1001/1007 */
export function sendSmsCode(phone, config) {
  return request.post('/users/sms-code', { phone }, config)
}

/** P-008 短信验证码登录：{phone, smsCode} → 同 P-002；错误 1001/2003（验证码错误或过期）/2004（账号禁用）；注：T5 文档列的 2005 为笔误，后端不产生 */
export function smsLogin(data, config) {
  return request.post('/users/login/sms', data, config)
}

/** U-001 当前用户资料：GET → data:{id,username,phone,role}；错误 1002 */
export function getProfile(config) {
  return request.get('/users/profile', config)
}

/** U-002 修改资料：{phone, oldPassword?, newPassword?}（改密时原密码错误抛 2003） */
export function updateProfile(data, config) {
  return request.put('/users/profile', data, config)
}
