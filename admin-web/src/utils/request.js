/**
 * 统一请求封装（C1 任务书 / .qoder/members/member-c/rules/admin-frontend.md 第 3 节）
 * - baseURL：/api（vite 代理到后端 8080）
 * - 请求拦截：注入 Authorization: Bearer <token>
 * - 响应拦截：统一解包 {code, message, data, total}；code 200 成功
 * - 错误码提示文案映射（T3 错误码分段表）集中在本文件，页面不重复定义
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：注入 token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// T3 错误码 → 提示文案（只列后台常用；未映射的走后端 message）
const CODE_MESSAGES = {
  1001: '参数错误',
  1002: '未登录或登录已过期',
  1003: '无权限访问',
  1004: '资源不存在',
  1005: '系统错误',
  6001: '当前账号尚无店铺',
  6002: '店铺尚未通过审核',
  6003: '店铺审核未通过',
  6004: '无权限访问该资源',
  6005: '店铺状态异常，暂不可操作',
  7001: '审核目标不存在',
  7002: '当前状态不可执行该审核操作',
  7005: '审核原因必填'
}

// 响应拦截：统一解包 + 错误提示
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      // 分页接口返回 {list, total}；普通接口返回 data
      return res.data
    }
    if (res.code === 1002) {
      // 未登录/过期：清登录态跳登录页
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_user')
      ElMessage.error(CODE_MESSAGES[res.code] || res.message || '未登录或登录已过期')
      router.replace('/login')
      return Promise.reject(res)
    }
    ElMessage.error(CODE_MESSAGES[res.code] || res.message || '操作失败')
    return Promise.reject(res)
  },
  (error) => {
    ElMessage.error('网络异常，请稍后重试')
    return Promise.reject(error)
  }
)

export default request
