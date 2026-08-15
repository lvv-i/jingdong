// Axios 统一封装（成员B）
// 依据：.qoder/members/member-b/skills/api-integration.md + .qoder/rules/api-contract.md
//
// 统一返回信封 {code, message, data, total}：
//   code === 200      → 成功，返回 data
//   code === 1002     → 未登录：清 token 跳 /login（全局处理）
//   其余业务错误       → ElMessage.error(message)，reject 供页面兜底
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器：注入 JWT（登录后所有请求携带 Authorization: Bearer <token>）
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：解包统一返回；1002 全局跳登录；其余业务错误 toast（silent 除外）
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 兜底：非标准信封直接透传
    if (res === null || typeof res !== 'object' || res.code === undefined) {
      return res
    }
    if (res.code === 200) {
      return res.data
    }
    // 未登录/账号禁用：清登录态跳登录页（不弹错误）
    if (res.code === 1002) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      if (!window.location.pathname.startsWith('/login')) {
        ElMessage.warning(res.message || '请先登录')
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.message || '未登录'))
    }
    // 业务错误：附带 code 供页面分支处理；silent=true 时不弹全局 toast（页面自处理，如登录/注册表单级提示）
    const err = new Error(res.message || '请求失败')
    err.code = res.code
    if (!response.config.silent) {
      ElMessage.error(res.message || '请求失败')
    }
    return Promise.reject(err)
  },
  (error) => {
    // 网络层错误（后端未启动/超时/500 等）；silent=true 时不弹全局 toast（页面自处理）
    const msg = error?.response?.data?.message || error.message || '网络异常，请稍后重试'
    if (!error?.config?.silent) {
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

export default request
