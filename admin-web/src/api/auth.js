/**
 * 公共/登录接口封装（T5 公共组）
 * 函数名 = T5 编号，注释标注编号与 URL（.qoder/members/member-c/skills/admin-page-dev.md 第 2 步）
 */
import request from '../utils/request'

// P-002 POST /api/users/login（商家/管理员共用登录入口）
export function P002_login(data) {
  return request.post('/users/login', data)
}

// P-007 POST /api/users/sms-code（短信验证码，demo 固定 123456）
export function P007_smsCode(data) {
  return request.post('/users/sms-code', data)
}

// P-008 POST /api/users/login/sms（验证码登录）
export function P008_smsLogin(data) {
  return request.post('/users/login/sms', data)
}

// P-003 GET /api/categories（类目树，白名单）
export function P003_categories() {
  return request.get('/categories')
}
