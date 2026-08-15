/**
 * 登录态存储（C2 任务书）
 * - token 与用户信息存 localStorage（刷新页面保持登录态）
 * - 角色：MERCHANT（商家）/ ADMIN（管理员），用于路由守卫与菜单渲染
 */
import { defineStore } from 'pinia'

const TOKEN_KEY = 'admin_token'
const USER_KEY = 'admin_user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    userInfo: JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    role: (state) => state.userInfo?.role || '',
    username: (state) => state.userInfo?.username || ''
  },
  actions: {
    // 登录成功：P-002 返回 {token, userInfo:{id,username,role,shopId}}
    setLogin(payload) {
      this.token = payload.token
      this.userInfo = payload.userInfo
      localStorage.setItem(TOKEN_KEY, payload.token)
      localStorage.setItem(USER_KEY, JSON.stringify(payload.userInfo))
    },
    // 退出登录
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }
})
